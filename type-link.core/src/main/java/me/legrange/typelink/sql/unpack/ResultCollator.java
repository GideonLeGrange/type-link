package me.legrange.typelink.sql.unpack;

import java.util.List;

interface ResultCollator {

    Object[] collate(List<?> row);

}
