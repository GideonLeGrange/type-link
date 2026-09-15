package me.legrange.typelink.lambda.parser;

import me.legrange.typelink.lambda.BytecodeParseException;
import me.legrange.typelink.lambda.ClassDecodingException;
import me.legrange.typelink.lambda.DecoderException;
import me.legrange.typelink.lambda.structure.*;

import java.lang.classfile.CodeElement;
import java.lang.classfile.CodeModel;
import java.lang.classfile.Instruction;
import java.lang.classfile.Label;
import java.lang.classfile.Opcode;
import java.lang.classfile.instruction.*;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.reflect.AccessFlag;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.String.format;
import static me.legrange.typelink.lambda.lookup.Constructors.lookupConstructor;
import static me.legrange.typelink.lambda.lookup.Static.lookupStatic;
import static me.legrange.typelink.lambda.lookup.Virtual.lookupVirtual;
import static me.legrange.typelink.lambda.parser.BranchParser.generateExpression;
import static me.legrange.typelink.lambda.parser.ClassUtil.*;
import static me.legrange.typelink.lambda.parser.Dereference.dereference;
import static me.legrange.typelink.lambda.parser.Flip.flip;

public final class BytecodeParser {

    private final List<CodeElement> elements;
    private final Map<Label, Integer> labels;
    private final Map<Integer, Reference> variables = new HashMap<>();
    private final Map<Reference, Value> heap = new HashMap<>();
    private final Set<Integer> fromEnclosingScope = new HashSet<>();
    private final Stack<Value> stack = new Stack<>();
    private int lineNumber;

    /**
     * @param capturedCount how many of the method's leading parameters the lambda captured, rather
     *                      than declaring. The rest are its own, and it is their position that says
     *                      which table in the query they stand for.
     */
    public static ByteCodeModel parseBytecode(CodeModel model, int capturedCount) {
        return new BytecodeParser(model, capturedCount, List.of()).parseBytecode();
    }

    /**
     * Parse a lambda that is nested inside another one, given the values it captured.
     *
     * <p>Only the outermost lambda is serialized, so only its captures can be read back from a
     * {@link java.lang.invoke.SerializedLambda}. A nested one is built at run time inside the
     * enclosing body, and its captures are whatever that frame pushed in front of the
     * {@code INVOKEDYNAMIC} - values this parser is already holding. Passing them in here puts them
     * straight into the nested frame's slots, so the nested body reads the enclosing scope's values
     * rather than parameters nothing can resolve.
     */
    private static ByteCodeModel parseBytecode(CodeModel model, List<Value> captured) {
        return new BytecodeParser(model, captured.size(), captured).parseBytecode();
    }

    private BytecodeParser(CodeModel model, int capturedCount, List<Value> capturedValues) {
        this.elements = model.elementList();
        this.labels = IntStream.range(0, elements.size())
                .filter(i -> elements.get(i) instanceof Label)
                .boxed()
                .collect(Collectors.toMap(i -> (Label) (elements.get(i)), i -> i));
        seedParameters(model, capturedCount, capturedValues);
    }

    /**
     * Seed the local variable slots from the enclosing method's descriptor.
     *
     * <p>The slot table was previously populated only from {@link LocalVariable} elements, which the
     * class file reader synthesises from the {@code LocalVariableTable} attribute. That attribute is
     * only emitted when compiling with {@code -g:vars}, so without it every argument load pushed
     * {@code null} and decoding failed far from the cause. The method descriptor carries the same
     * types unconditionally, so it is the better source; {@link #variable(LocalVariable)} still runs
     * afterwards where the attribute is present, and refines these entries with the real names.
     *
     * <p>Each entry also records its position in the parameter list. That position, not the slot,
     * is what indexes the lambda's captured arguments: a {@code long} or {@code double} parameter
     * occupies two slots, so slot numbers run ahead of positions as soon as a wide capture appears.
     */
    private void seedParameters(CodeModel model, int capturedCount, List<Value> capturedValues) {
        var method = model.parent();
        if (method.isEmpty()) {
            return;
        }
        var descriptor = method.get().methodTypeSymbol();
        var slot = 0;
        var index = 0;
        if (!method.get().flags().has(AccessFlag.STATIC)) {
            // Slot 0 holds `this`. A method reference to an instance method - a record
            // accessor, say - decodes to that method's own body, which loads it. Where the
            // lambda captures its receiver, that receiver is also captured argument 0.
            var owner = method.get().parent();
            owner.ifPresent(classElements -> seed(0, 0, -capturedCount, classForDesc(classElements.thisClass().asSymbol()),
                    "this", capturedValues));
            slot = 1;
            index = 1;
        }
        for (var parameter : descriptor.parameterList()) {
            seed(slot, index, index - capturedCount, classForDesc(parameter), "arg" + index, capturedValues);
            // long and double occupy two slots each, but still advance the position by one.
            slot += isWide(parameter) ? 2 : 1;
            index++;
        }
    }

    /**
     * Seed one slot, with the enclosing scope's value where there is one for it.
     *
     * <p>{@code capturedValues} is empty for the outermost lambda, whose captures stay as
     * {@link Argument}s to be looked up in the serialized form later. It is populated for a nested
     * one, and then the leading parameters - the captures - hold the enclosing frame's values
     * instead.
     */
    private void seed(int slot, int index, int rowPosition, Class<?> type, String name,
                      List<Value> capturedValues) {
        if (index < capturedValues.size()) {
            var reference = new Reference(slot, type);
            heap.put(reference, enclosing(capturedValues.get(index)));
            variables.put(slot, reference);
            fromEnclosingScope.add(slot);
            return;
        }
        seed(slot, index, rowPosition, type, name);
    }

    private void seed(int slot, int index, int rowPosition, Class<?> type, String name) {
        var reference = new Reference(slot, type);
        heap.put(reference, new Argument(slot, index, rowPosition, 0, name, type));
        variables.put(slot, reference);
    }

    /**
     * A value as the nested scope should see it.
     *
     * <p>Where the enclosing lambda's own row parameter is captured - the usual way a sub-select
     * correlates - it keeps its position and moves one scope further out. Its position means the
     * table it came from, which is in the enclosing query's FROM and not in this one's, so the two
     * have to be told apart.
     *
     * <p>Reading it off the declared type instead would be simpler and is wrong: a predicate
     * written over a type variable erases its row to the bound, which names no table at all.
     */
    private static Value enclosing(Value value) {
        return value instanceof Argument arg && arg.isRowParameter()
                ? new Argument(arg.slot(), arg.index(), arg.rowPosition(), arg.scopeDepth() + 1,
                        arg.name(), arg.type())
                : value;
    }

    private static boolean isWide(ClassDesc desc) {
        return ConstantDescs.CD_long.equals(desc) || ConstantDescs.CD_double.equals(desc);
    }

    private ByteCodeModel parseBytecode() {
        var code = parseBytecode(0);
        if (!stack.isEmpty()) {
            throw bug("Stack not empty at end of processing (%d elements)", stack.size());
        }
        return code;
    }

    private ByteCodeModel parseBytecode(int pos) {
        for (var i = pos; i < elements.size(); ++i) {
            var code = elements.get(i);
            switch (code) {
                case LocalVariable lv:
                    variable(lv);
                    break;
                case LocalVariableType _, Label _:
                    break;
                case LineNumber ln:
                    lineNumber = ln.line();
                    break;
                case LoadInstruction li:
                    load(li);
                    break;
                case InvokeInstruction ii:
                    invoke(ii);
                    break;
                case InvokeDynamicInstruction idi:
                    invokeDynamic(idi);
                    break;
                case BranchInstruction bi:
                    return branch(bi, i);
                case ConstantInstruction ci:
                    constant(ci);
                    break;
                case FieldInstruction fi:
                    field(fi);
                    break;
                case ReturnInstruction ri:
                    return retrn(ri);
                case StoreInstruction si:
                    store(si);
                    break;
                case OperatorInstruction oi:
                    operator(oi);
                    break;
                case ConvertInstruction ci:
                    convert(ci);
                    break;
                case NewObjectInstruction noi:
                    newObject(noi);
                    break;
                case StackInstruction si:
                    stack(si);
                    break;
                case TypeCheckInstruction _:
                    break;
                default:
                    throw bug("Unsupported value element %s", code);
            }
        }
        throw bug("No branch or return in value");
    }

    private void variable(LocalVariable lv) throws DecoderException {
        if (fromEnclosingScope.contains(lv.slot())) {
            // Holds a value from the scope around this lambda, not a parameter of its own; there is
            // no position or name here worth refining, and reseeding would discard the value.
            return;
        }
        // Parameters are already seeded from the descriptor; this refines them with the real
        // name while keeping the parameter position, which the attribute does not carry. A slot
        // with no seeded entry is a local declared inside the body, never a captured argument.
        var seeded = heap.get(variables.get(lv.slot()));
        var index = seeded instanceof Argument arg ? arg.index() : -1;
        var rowPosition = seeded instanceof Argument arg ? arg.rowPosition() : -1;
        seed(lv.slot(), index, rowPosition, classForDesc(lv.typeSymbol()), lv.name().stringValue());
    }

    private void stack(StackInstruction si) {
        switch (si.opcode()) {
            case DUP -> {
                var val = pop();
                push(val);
                push(val);
            }
            // A value the compiler discards is one this stack should drop too - javac emits these
            // for an expression whose result goes unused, and for the 'this' in front of a constant
            // variable it has already inlined.
            //
            // POP2 drops one value here rather than two. It has two forms: one category-2 value, or
            // two category-1 ones. A long or double occupies a single entry on this stack (LCONST_0
            // sits with the ICONST_n in constant(), and LCMP pops two operands, not four), and javac
            // only ever emits the wide form, so the two opcodes do the same thing.
            case POP, POP2 -> pop();
            default -> throw unsupported(si.opcode());
        }
    }

    private void newObject(NewObjectInstruction noi) {
        if (noi.opcode() == Opcode.NEW) {
            try {
                push(new NewObject(Class.forName(noi.className().name().stringValue().replace('/', '.')), List.of()));
            } catch (ClassNotFoundException e) {
                throw new ClassDecodingException(e.getMessage(), e);
            }
        } else {
            throw unsupported(noi.opcode());
        }
    }

    private void store(StoreInstruction si) throws DecoderException {
        switch (si.opcode()) {
            case ASTORE_1, ISTORE_2 -> heap.put(variables.get(si.slot()), pop());
            default -> throw unsupported(si.opcode());
        }
    }

    private ByteCodeModel retrn(ReturnInstruction ri) throws DecoderException {
        return switch (ri.opcode()) {
            case ARETURN -> returnRef(pop());
            case IRETURN, DRETURN -> returnVal(pop());
            case RETURN -> returnRef(variables.get(0));
            default -> throw unsupported(ri.opcode());
        };
    }

    private ByteCodeModel returnRef(Value value) {
        value = dereference(value, heap::get);
        if (value instanceof NewObject(var type, var fields)) {
            value = dereference(new ConstructorCall(type, getConstructor(type, fields.size()), fields), heap::get);
        }
        return new Return(dereference(value, heap::get));
    }

    private ByteCodeModel returnVal(Value val) {
        return switch (val) {
            case Constant<?> constant -> returnRef(constant);
            case Evaluation eval -> branch(flip(eval), new True(), new False());
            case MethodCall mc -> branch(new Eq(mc, new Constant<>(false)), new True(), new False());
            case InstanceFieldReference field -> returnRef(field);
            default ->
                    throw bug("Expected constant or expression on stack but found %s", val.getClass().getSimpleName());
        };
    }

    private Branch branch(Expression eval, Flow jump, Flow cont) {
        return new Branch(dereference(eval, heap::get), jump, cont);
    }

    private void field(FieldInstruction fi) throws DecoderException {
        switch (fi.opcode()) {
            case GETSTATIC -> {
                var field = fieldForRef(fi.field());
                push(new StaticFieldReference(field));
            }
            case GETFIELD -> {
                var field = fieldForRef(fi.field());
                push(new InstanceFieldReference(pop(), field));
            }
            case PUTFIELD -> {
                var value = pop();
                var target = pop();
                if (target instanceof Reference ref) {
                    var object = dereference(ref, heap::get);
                    if (object instanceof NewObject(var type, var fields)) {
                        heap.put(ref, new NewObject(type, Stream.concat(fields.stream(), Stream.of(value)).toList()));
                    } else {
                        throw bug("Don't know how to apply %s to %s", fi.opcode(), target.getClass().getSimpleName());
                    }
                } else {
                    throw bug("Don't know how to apply %s to %s", fi.opcode(), target.getClass().getSimpleName());
                }
            }
            default -> throw unsupported(fi.opcode());
        }
    }

    private void operator(OperatorInstruction oi) throws DecoderException {
        switch (oi.opcode()) {
            case LCMP, DCMPG, DCMPL, FCMPG, FCMPL -> {
                return;
            }
        }
        var right = pop();
        var left = pop();
        push(switch (oi.opcode()) {
            case FADD -> new Add(Float.class, left, right);
            case FSUB -> new Subtract(Float.class, left, right);
            case FMUL -> new Multiply(Float.class, left, right);
            case FDIV -> new Divide(Float.class, left, right);
            case DADD -> new Add(Double.class, left, right);
            case DMUL -> new Multiply(Double.class, left, right);
            case DSUB -> new Subtract(Double.class, left, right);
            case DDIV -> new Divide(Double.class, left, right);
            case IADD -> new Add(Integer.class, left, right);
            case ISUB -> new Subtract(Integer.class, left, right);
            case IMUL -> new Multiply(Integer.class, left, right);
            case IDIV -> new Divide(Integer.class, left, right);
            case LADD -> new Add(Long.class, left, right);
            case LSUB -> new Subtract(Long.class, left, right);
            case LMUL -> new Multiply(Long.class, left, right);
            case LDIV -> new Divide(Long.class, left, right);
            default -> throw unsupported(oi.opcode());
        });
    }

    private void convert(ConvertInstruction ignored) {
        push(pop()); // We can add conversions in here
    }

    private void constant(ConstantInstruction ci) throws DecoderException {
        switch (ci.opcode()) {
            case ICONST_M1, ICONST_0, ICONST_1, ICONST_2, ICONST_3, ICONST_4, ICONST_5,
                 LCONST_0, LCONST_1,
                 // The compiler emits these in place of a constant pool entry for the handful of
                 // literals that have their own opcode. Their absence made a predicate as ordinary
                 // as "amount > 0.0" fail to decode while "amount > 2.0" was fine.
                 FCONST_0, FCONST_1, FCONST_2, DCONST_0, DCONST_1,
                 LDC, LDC_W, LDC2_W, BIPUSH, SIPUSH ->
                    push(new Constant<>(constForDesc(ci.constantValue())));
            case ACONST_NULL -> push(new Constant<>(null));
            default -> throw unsupported(ci.opcode());
        }
    }

    private ByteCodeModel branch(BranchInstruction bi, int pos) throws DecoderException {
        if (bi.opcode() == Opcode.GOTO || bi.opcode() == Opcode.GOTO_W) {
            return parseBytecode(indexOf(bi.target()));
        }
        var eval = switch (bi.opcode()) {
            case IFNULL, IFNONNULL -> branch1(bi.opcode());
            case IFEQ, IFNE, IFGE, IFGT, IFLE, IFLT -> branchFromStack(bi, followsComparison(pos));
            case IF_ACMPNE, IF_ICMPNE, IF_ICMPGT, IF_ICMPLT, IF_ICMPGE, IF_ICMPLE,
                 IF_ICMPEQ, IF_ACMPEQ -> branch2(bi.opcode());
            default -> throw unsupported(bi.opcode());
        };
        return branch(eval, flow(parseBytecode(pos + 1)), flow(parseBytecode(indexOf(bi.target()))));
    }

    private Flow flow(ByteCodeModel model) {
        return switch (model) {
            case Branch jump -> jump;
            case Return(Constant<?>(Integer i)) when i == 1 -> new True();
            case Return(Constant<?>(Integer i)) when i == 0 -> new False();
            case Return(Value value) -> throw bug("Unexpected return value for jump %s", value);
        };
    }

    private int indexOf(Label label) {
        return labels.get(label);
    }

    private Evaluation branch1(Opcode opcode) {
        var val = pop();
        return switch (opcode) {
            case IFNULL -> new IsNull(val);
            case IFNONNULL -> new IsNotNull(val);
            default -> throw unsupported(opcode);
        };
    }

    private Evaluation branch2(Opcode opcode) {
        var right = pop();
        var left = pop();
        if (left.type() == Boolean.class || left.type() == Boolean.TYPE) {
            if (right instanceof Constant<?>(Integer i)) {
                right = new Constant<>(i != 0);
            }
        }
        return switch (opcode) {
            case IF_ACMPNE, IF_ICMPNE -> new Neq(left, right);
            case IF_ICMPEQ, IF_ACMPEQ -> new Eq(left, right);
            case IF_ICMPGE -> new Ge(left, right);
            case IF_ICMPLE -> new Le(left, right);
            case IF_ICMPLT -> new Lt(left, right);
            case IF_ICMPGT -> new Gt(left, right);
            default -> throw unsupported(opcode);
        };
    }

    /**
     * Whether the instruction preceding position {@code pos} is a comparison.
     *
     * <p>{@code LCMP} and the float and double comparisons are treated as no-ops that leave both
     * of their operands on the stack, so a branch following one has two values to work with. A
     * branch with no comparison in front of it is testing a single int-shaped value against an
     * implicit zero, and has only one.
     *
     * <p>This is read from the instruction stream rather than inferred from the operand's type,
     * because {@link #convert(ConvertInstruction)} discards conversions: an {@code int} widened to
     * {@code double} by {@code I2D} still models as an Integer, so type tells us nothing reliable
     * about what the stack actually holds.
     */
    private boolean followsComparison(int pos) {
        for (var i = pos - 1; i >= 0; i--) {
            // Labels, line numbers and local variable declarations sit between instructions.
            if (elements.get(i) instanceof Instruction instruction) {
                return switch (instruction.opcode()) {
                    case LCMP, DCMPG, DCMPL, FCMPG, FCMPL -> true;
                    default -> false;
                };
            }
        }
        return false;
    }

    private Expression branchFromStack(BranchInstruction bi, boolean followsComparison) throws DecoderException {
        var call = pop();
        return switch (call) {
            case MethodCall mc when mc.type().isAssignableFrom(Boolean.TYPE) -> switch (bi.opcode()) {
                case IFEQ -> new Eq(mc, new Constant<>(true));
                case IFNE -> new Neq(mc, new Constant<>(false));
                default -> throw unsupported(bi.opcode());
            };
            case Expression eval -> switch (bi.opcode()) {
                case IFEQ -> flip(eval);
                case IFNE, IFGT, IFLT, IFLE, IFGE -> eval;
                default -> throw unsupported(bi.opcode());
            };
            // Two operands are on the stack when a comparison instruction preceded this branch;
            // LCMP and friends are treated as no-ops that leave them there. A branch on an int
            // has no such instruction in front of it - the comparison is against an implicit
            // zero, and the other operand does not exist. Popping for it emptied the stack, which
            // is why "num == 0" failed to decode while "num == 1" was fine.
            default -> {
                // Operand order matters and differs between the two cases. After a comparison
                // instruction the stack holds [left, right] and `call` is the right-hand one. For
                // an implicit-zero branch `call` is the left-hand operand - the thing being
                // compared - and the zero belongs on the right. Swapping them silently inverts
                // every ordered comparison: "num > 0" becomes "0 > num".
                Value left;
                Value right;
                if (followsComparison) {
                    left = pop();
                    right = call;
                } else {
                    left = call;
                    right = new Constant<>(0);
                }
                yield switch (bi.opcode()) {
                    case IFEQ -> new Eq(left, right);
                    case IFNE -> new Neq(left, right);
                    case IFLE -> new Le(left, right);
                    case IFLT -> new Lt(left, right);
                    case IFGE -> new Ge(left, right);
                    case IFGT -> new Gt(left, right);
                    default -> throw unsupported(bi.opcode());
                };
            }
        };
    }


    private void invoke(InvokeInstruction ii) throws DecoderException {
        switch (ii.opcode()) {
            case INVOKEVIRTUAL, INVOKEINTERFACE -> invokeVirtual(ii);
            case INVOKESTATIC -> invokeStatic(ii);
            case INVOKESPECIAL -> invokeSpecial(ii);
            default -> throw unsupported(ii.opcode());
        }
    }

    private void invokeDynamic(InvokeDynamicInstruction idi) throws DecoderException {
        if (idi.opcode() != Opcode.INVOKEDYNAMIC) {
            throw unsupported(idi.opcode());
        }
        var bsm = idi.bootstrapMethod();
        var bsmOwner = bsm.owner();
        var bsmMethod = bsm.methodName();
        // Check if it's a lambda metafactory
        if (bsmOwner.equals(ClassDesc.of("java.lang.invoke.LambdaMetafactory"))
                && (bsmMethod.equals("metafactory") || bsmMethod.equals("altMetafactory"))) {
            parseLambda(idi);
        } else if (bsmOwner.equals(ClassDesc.of("java.lang.invoke.StringConcatFactory"))
        && (bsmMethod.equals("makeConcatWithConstants"))) {
            parseStringConcat(idi);
        }
        else {
            throw bug("Don't know how to invoke %s", bsmMethod);
        }
    }

    private void parseLambda(InvokeDynamicInstruction idi) {
        var args = idi.bootstrapArgs();
        if (args.size() >= 3) {
            var implHandle = (DirectMethodHandleDesc) args.get(1); // Handle to lambda body
            Method method;
            try {
                var lookup = MethodHandles.privateLookupIn(classForDesc(implHandle.owner()), MethodHandles.lookup());
                method = lookup.revealDirect(implHandle.resolveConstantDesc(lookup)).reflectAs(Method.class, lookup);
                // Flags (if altMetafactory)
                var flags = (args.size() > 3) ? (Integer) args.get(3) : 0;
                var isSerializable = (flags & LambdaMetafactory.FLAG_SERIALIZABLE) != 0;
                if (isSerializable) {
                    // The indy call site takes the captured values as its arguments, so its
                    // parameter count is how many of the body's leading parameters are captures.
                    var capturedCount = idi.typeSymbol().parameterCount();
                    // The captured values sit on the stack in front of the call site, in parameter
                    // order, and are what the nested body's leading parameters stand for. Resolve
                    // them against this frame first: a slot load leaves a Reference on the stack,
                    // and a reference into this frame means nothing in the nested one.
                    var captured = pop(capturedCount).reversed().stream()
                            .map(value -> dereference(value, heap::get))
                            .toList();
                    var code = parseBytecode(findCodeModel(method), captured);
                    push(switch (code) {
                        case Branch jump -> generateExpression(jump);
                        case Return aReturn -> aReturn.value();
                    });
                } else {
                    push(new MethodReference(method));
                }
            } catch (ReflectiveOperationException e) {
                throw new ClassDecodingException(e.getMessage(), e);
            }
        }
        else {
            throw new ClassDecodingException("Don't know how parse lambda. BUG!");
        }
    }

    private void parseStringConcat(InvokeDynamicInstruction idi) throws DecoderException {
        // Get the number of dynamic arguments from the method signature
        var paramCount = idi.typeSymbol().parameterCount();
        
        // Pop dynamic values from the stack (in reverse order)
        var dynamicValues = pop(paramCount).reversed();
        
        // Get the recipe string from bootstrap arguments
        // The recipe describes how to interleave constants and dynamic values
        // \u0001 or \u0002 represents placeholders for dynamic arguments
        var args = idi.bootstrapArgs();
        var recipe = (String) args.getFirst();
        // Build the concatenation result
        var params = new ArrayList<Value>();
        var text = new StringBuilder();
        var j = 0;
        for (int i = 0; i < recipe.length(); i++) {
            char ch = recipe.charAt(i);
            // Check if this is a placeholder for a dynamic argument
            if (ch == '\u0001' || ch == '\u0002') {
                if (!text.isEmpty()) {
                    params.add(new Constant<>(text.toString()));
                    text = new StringBuilder();
                }
                params.add(dynamicValues.get(j));
                j++;
            }
            else {
                text.append(ch);
            }
        }
        if (!text.isEmpty()) {
            params.add(new Constant<>(text.toString()));
        }
        push(new Concat(params));
    }

    private void invokeSpecial(InvokeInstruction ii) {
        var parCount = ii.typeSymbol().parameterCount();
        var pars = pop(parCount).reversed();
        var target = pop();
        var methodName = ii.method().name().stringValue();
        if (!methodName.equals("<init>")) {
            throw bug("Don't know how to call %s on %s", methodName, target.getClass().getSimpleName());
        }
        var constructor = getConstructor(classForDesc(ii.owner().asSymbol()), parCount);
        if (target instanceof NewObject(Class<?> type, _)) {
            pop(); // hack
            var opt = lookupConstructor(constructor);
            if (opt.isPresent()) {
                push(opt.get().apply(pars));
            } else {
                push(new ConstructorCall(type, constructor, pars));
            }
        } else if (target instanceof Reference ref) {
            var object = dereference(ref, heap::get);
            switch (object) {
                case null -> throw bug("Can't call %s on null", methodName);
                case NewObject(var type, var fields) -> heap.put(ref, new NewObject(type, fields));
                case Argument arg -> heap.put(ref, new NewObject(arg.type(), List.of()));
                default -> throw bug("Don't know how to call %s on %s", ii.opcode(), object.getClass().getSimpleName());
            }
        } else {
            throw bug("Don't know how to call %s on %s", ii.opcode(), target.getClass().getSimpleName());
        }
    }

    private void invokeVirtual(InvokeInstruction ii) throws DecoderException {
        var parCount = ii.typeSymbol().parameterCount();
        var params = pop(parCount);
        var method = getMethod(classForDesc(ii.owner().asSymbol()), ii.method().name().stringValue(), params);
        var opt = lookupVirtual(method);
        if (opt.isPresent()) {
            var target = pop();
            push(opt.get().apply(Stream.concat(Stream.of(target), params.stream()).toList()));
        } else {
            push(new MethodCall(pop(), method, params));
        }
    }

    private void invokeStatic(InvokeInstruction ii) throws DecoderException {
        var parCount = ii.typeSymbol().parameterCount();
        var pars = pop(parCount);

        var method = getMethod(classForDesc(ii.owner().asSymbol()), ii.method().name().stringValue(), pars);
        var opt = lookupStatic(method);
        if (opt.isPresent()) {
            push(opt.get().apply(pars));
        } else {
            push(new StaticMethodCall(method, pars));
        }
    }

    private void load(LoadInstruction li) throws DecoderException {
        push(variables.get(li.slot()));
    }

    private Constructor<?> getConstructor(Class<?> type, int paramCount) {
        return Stream.of(type.getDeclaredConstructors())
                .filter(c -> c.getParameterCount() == paramCount)
                .findFirst()
                .orElseThrow(() -> new ClassDecodingException(format("No constructor for %s with %d parameters", type.getName(), paramCount)));
    }

    private Method getMethod(Class<?> type, String name, List<Value> params) {
        return findMethod(type, name, params)
                .orElseThrow(() -> new ClassDecodingException(format("No method %s with %d parameters", name, params.size())));
    }

    private Optional<Method> findMethod(Class<?> type, String name, List<Value> params) {
        var best = Stream.of(type.getDeclaredMethods())
                .filter(m -> m.getName().equals(name))
                .filter(m -> m.getParameterCount() == params.size())
                .toList();

        return switch (best.size()) {
            case 0 -> {
                // Try interfaces first
                var inInterface = findMethodInInterfaces(type, name, params);
                if (inInterface.isPresent()) {
                    yield inInterface;
                }
                // Try superclass
                var superClass = type.getSuperclass();
                if (superClass != null && !superClass.equals(Object.class)) {
                    yield findMethod(superClass, name, params);
                }
                yield Optional.empty();
            }
            case 1 -> best.stream().findFirst();
            default -> selectBestMethod(best, params);

        };
    }

    private Optional<Method> findMethodInInterfaces(Class<?> type, String name, List<Value> params) {
        for (var iface : type.getInterfaces()) {
            var opt = findMethod(iface, name, params);
            if (opt.isPresent()) {
                return opt;
            }
        }
        return Optional.empty();
    }

    private Optional<Method> selectBestMethod(List<Method> best, List<Value> params) {
        // Find the method where all parameter types match, considering primitive/boxed equivalence
        var exactMatches = best.stream()
                .filter(method -> parametersMatch(method, params))
                .toList();

        if (!exactMatches.isEmpty()) {
            return exactMatches.stream().findFirst();
        }

        // If no exact match found, try to find a method where parameters are assignable
        var assignableMatches = best.stream()
                .filter(method -> parametersAssignable(method, params))
                .toList();

        if (!assignableMatches.isEmpty()) {
            return assignableMatches.stream().findFirst();
        }

        // If still no match, return the first method (handles generic methods with type erasure)
        return best.stream().findFirst();
    }

    private boolean parametersMatch(Method method, List<Value> params) {
        var paramTypes = method.getParameterTypes();
        if (paramTypes.length != params.size()) {
            return false;
        }
        for (int i = 0; i < paramTypes.length; i++) {
            if (!typesMatch(paramTypes[i], params.get(i).type())) {
                return false;
            }
        }
        return true;
    }

    private boolean parametersAssignable(Method method, List<Value> params) {
        var paramTypes = method.getParameterTypes();
        if (paramTypes.length != params.size()) {
            return false;
        }
        for (int i = 0; i < paramTypes.length; i++) {
            if (!isAssignable(paramTypes[i], params.get(i).type())) {
                return false;
            }
        }
        return true;
    }

    private boolean typesMatch(Class<?> methodParamType, Class<?> valueType) {
        // Exact match
        if (methodParamType.equals(valueType)) {
            return true;
        }
        // Check if one is primitive and the other is its boxed type
        if (methodParamType.isPrimitive()) {
            return getBoxedType(methodParamType).equals(valueType);
        }
        if (valueType.isPrimitive()) {
            return getBoxedType(valueType).equals(methodParamType);
        }
        return false;
    }

    private boolean isAssignable(Class<?> methodParamType, Class<?> valueType) {
        // Handle primitive/boxed conversions
        var methodType = methodParamType.isPrimitive() ? getBoxedType(methodParamType) : methodParamType;
        var valType = valueType.isPrimitive() ? getBoxedType(valueType) : valueType;
        // Check if methodParamType is assignable from valueType
        return methodType.isAssignableFrom(valType);
    }

    private Class<?> getBoxedType(Class<?> primitiveType) {
        if (primitiveType == int.class) return Integer.class;
        if (primitiveType == long.class) return Long.class;
        if (primitiveType == double.class) return Double.class;
        if (primitiveType == float.class) return Float.class;
        if (primitiveType == boolean.class) return Boolean.class;
        if (primitiveType == byte.class) return Byte.class;
        if (primitiveType == char.class) return Character.class;
        if (primitiveType == short.class) return Short.class;
        if (primitiveType == void.class) return Void.class;
        return primitiveType;
    }

    private List<Value> pop(int parCount) {
        return IntStream.range(0, parCount)
                .mapToObj(_ -> pop())
                .toList();
    }

    private Value pop() {
        return stack.pop();
    }

    private void push(Value value) {
        stack.push(value);
    }

    private DecoderException unsupported(Opcode opcode) {
        return bug("Unsupported opcode %s", opcode);
    }

    private DecoderException bug(String fmt, Object... args) {
        return new BytecodeParseException(lineNumber, format(fmt, args) + ". BUG!");
    }


}