package org.limbo.flowjob.broker.core.dispatch;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.core.worker.Worker;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * 根据标签过滤
 *
 * @author Brozen
 * @since 2022-12-14
 */
@Builder
@Setter(AccessLevel.NONE)
public class TagFilterOption {

    /**
     * 标签名
     */
    public final String tagName;

    /**
     * 标签值
     */
    public final String tagValue;

    /**
     * 匹配条件
     */
    public final Condition condition;


    /**
     * 过滤 Worker，判断是否符合条件。
     */
    public Predicate<Worker> asPredicate() {
        return worker -> {
            Map<String, List<String>> tags = worker.getTags();

            List<String> values = tags.get(this.tagName);
            switch (this.condition) {
                case EXISTS:
                    return CollectionUtils.isNotEmpty(values);

                case NOT_EXISTS:
                    return CollectionUtils.isEmpty(values);

                case MUST_MATCH_VALUE:
                    return CollectionUtils.isNotEmpty(values) && values.contains(this.tagValue);

                case MUST_NOT_MATCH_VALUE:
                    return CollectionUtils.isNotEmpty(values) && !values.contains(this.tagValue);

                case MUST_MATCH_VALUE_REGEX:
                    Pattern pattern = Pattern.compile(this.tagValue);
                    return CollectionUtils.isNotEmpty(values) && values.stream().anyMatch(s -> pattern.matcher(s).find());

                default:
                    return false;
            }
        };
    }


    enum Condition {

        /**
         * 存在指定名称的标签
         */
        EXISTS,

        /**
         * 不存在指定名称的标签
         */
        NOT_EXISTS,

        /**
         * 存在指定名称的标签且匹配指定值
         */
        MUST_MATCH_VALUE,

        /**
         * 存在指定名称的标签，且不匹配指定值
         */
        MUST_NOT_MATCH_VALUE,

        /**
         * 存在指定名称的标签且匹配正则表达式
         */
        MUST_MATCH_VALUE_REGEX

    }

}
