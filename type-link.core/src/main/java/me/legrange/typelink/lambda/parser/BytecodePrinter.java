package me.legrange.typelink.lambda.parser;

import java.lang.classfile.CodeElement;
import java.lang.classfile.CodeModel;
import java.lang.classfile.Label;
import java.lang.classfile.instruction.*;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

final class BytecodePrinter {

    private final CodeModel model;
    private final Map<Label, Integer> labels;
    private final Map<Integer, String> locals = new HashMap<>();

    public BytecodePrinter(CodeModel model) {
        this.model = model;
        this.labels = findLabels(model);
    }

    static void printModel(CodeModel model) {
        new BytecodePrinter(model).print();
    }

    private static Map<Label, Integer> findLabels(CodeModel model) {
        var codes = model.elementList();
        var res = new HashMap<Label, Integer>();
        for (var i = 0; i < codes.size(); ++i) {
            var code = codes.get(i);
            if (code instanceof Label label) {
                res.put(label, i);
            }
        }
        return res;
    }

    void print(CodeElement code) {
        System.out.println(switch (code) {
            case Label label -> asString(label) + ":";
            case LineNumber lineNumber -> asString(lineNumber);
            case LocalVariable lv -> {
                locals.put(lv.slot(), lv.name().stringValue());
                yield asString(lv);
            }
            case LoadInstruction ld -> asString(ld);
            case InvokeInstruction ii -> asString(ii);
            case BranchInstruction bi -> asString(bi);
            case StoreInstruction si -> asString(si);
            case ConstantInstruction ci -> asString(ci);
            case ReturnInstruction ri -> asString(ri);
            case FieldInstruction fi -> asString(fi);
            case OperatorInstruction oi -> asString(oi);
            case ConvertInstruction ci -> asString(ci);
            case LocalVariableType lvt -> asString(lvt);
            case InvokeDynamicInstruction idi -> asString(idi);
            case NewObjectInstruction noi ->
                    format("%s %s", noi.opcode(), noi.className().name().stringValue().replace('/', '.'));
            case StackInstruction si -> format("%s", si.opcode());
            default -> "\t" + code;
        });
    }

    private void print() {
        for (var code : model.elementList()) {
            print(code);
        }

    }

    private String asString(FieldInstruction fi) {
        return format("%s %s %s",
                fi.opcode(),
                fi.owner().name().stringValue().replace('/', '.'),
                fi.field().name());
    }

    private String asString(OperatorInstruction oi) {
        return format("%s", oi.opcode());
    }

    private String asString(ConvertInstruction ci) {
        return format("%s %s %s", ci.opcode(), ci.fromType(), ci.toType());
    }

    private String asString(ReturnInstruction ri) {
        return format("%s ", ri.opcode());
    }

    private String asString(ConstantInstruction ci) {
        return format("%s %s", ci.opcode(), ci.constantValue());
    }

    private String asString(BranchInstruction bi) {
        return format("%s %s", bi.opcode(), asString(bi.target()));
    }

    private String asString(StoreInstruction si) {
        return format("%s %d ; %s", si.opcode(), si.slot(), locals.get(si.slot()));
    }

    private String asString(LocalVariableType lvt) {
        return format("TYPE %d %s", lvt.slot(), lvt.name());
    }

    private String asString(InvokeInstruction ii) {
        return format("%s %s.%s ", ii.opcode(),
                ii.owner().name().stringValue().replace('/', '.')
                , ii.method().name());
    }

    private String asString(LoadInstruction ld) {
        return format("%s %d ; %s ", ld.opcode(), ld.slot(), locals.get(ld.slot()));
    }

    private String asString(LocalVariable lv) {
        return format("LOCAL %d %s %s", lv.slot(), lv.typeSymbol().displayName(), lv.name().stringValue());
    }

    private String asString(InvokeDynamicInstruction id) {
        return format("%s %s %s", id.opcode(), id.type(), id.name());
    }

    private String asString(LineNumber lineNumber) {
        return "; " + lineNumber.line();
    }

    private String asString(Label label) {
        return "L" + labels.get(label);
    }
}
