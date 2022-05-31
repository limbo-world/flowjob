package org.limbo.flowjob.broker.core.utils.strategies;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * @author Brozen
 * @since 2021-10-20
 */
public abstract class AbstractStrategyFactory<ST, S extends Strategy<T, R>, T, R> implements StrategyFactory<ST, S, T, R> {


    /**
     * 策略类型和策略生成器直接的映射
     */
    private ConcurrentHashMap<ST, Supplier<S>> strategyCreators;


    /**
     * 当策略类型没有匹配的策略生成器时，使用此策略生成器来生成策略
     */
    private Supplier<S> defaultStrategyCreator;


    public AbstractStrategyFactory() {
        this(null);
    }


    protected AbstractStrategyFactory(Supplier<S> defaultStrategyCreator) {
        this.strategyCreators = new ConcurrentHashMap<>();
        this.defaultStrategyCreator = defaultStrategyCreator;
    }


    /**
     * 注册策略生成器，同一个策略类型，重复注册只有最后一次的注册生效
     * @param type 策略类型
     * @param creator 策略生成器
     */
    protected void registerStrategyCreator(ST type, Supplier<S> creator) {
        this.strategyCreators.put(type, creator);
    }


    /**
     * 设置默认的策略生成器
     */
    protected void setDefaultStrategyCreator(Supplier<S> defaultStrategyCreator) {
        this.defaultStrategyCreator = defaultStrategyCreator;
    }

    /**
     * {@inheritDoc}
     * @param strategyType 策略创建的依据
     * @return
     */
    @Override
    public S newStrategy(ST strategyType) {
        Supplier<S> strategyCreator = strategyCreators.get(strategyType);
        if (strategyCreator == null) {
            strategyCreator = defaultStrategyCreator;
        }

        if (strategyCreator == null) {
            throw new IllegalStateException("No strategy provider for type " + strategyType);
        }

        return strategyCreator.get();
    }

}
