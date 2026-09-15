package me.legrange.typelink;

import java.io.Serializable;

public sealed interface SelectFunction extends Serializable permits SelectFunction1, SelectFunction2, SelectFunction3 {
}
