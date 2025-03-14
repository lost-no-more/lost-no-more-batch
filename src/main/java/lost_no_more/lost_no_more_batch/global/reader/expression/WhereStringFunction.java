package lost_no_more.lost_no_more_batch.global.reader.expression;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.StringPath;

@FunctionalInterface
public interface WhereStringFunction {
    BooleanExpression apply(StringPath id, int page, String currentId);
}