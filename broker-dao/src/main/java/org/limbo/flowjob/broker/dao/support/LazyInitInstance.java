/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.broker.dao.support;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * 只能处理接口代理
 *
 * @author Brozen
 * @since 2022-09-21
 */
@Deprecated
public class LazyInitInstance implements InvocationHandler {

    static final int S_UNAVAILABLE = 1;
    static final int S_INITIALIZING = 2;
    static final int S_AVAILABLE = 3;

    /**
     * 真实被代理的对象
     */
    private volatile Object delegated;

    /**
     * 用于初始化被代理对象
     */
    private Supplier<?> initializer;


    private AtomicInteger status;

    /**
     * 使用指定的初始化器，创建一个懒加载对象的代理处理器。
     */
    public LazyInitInstance(Supplier<?> initializer) {
        this.initializer = initializer;
        this.status = new AtomicInteger(S_UNAVAILABLE);
    }


    /**
     * 使用指定的初始化器，创建一个懒加载对象的代理处理器，返回指定类型的代理对象。
     * 调用返回的代理对象的任何方法都会触发初始化动作。
     */
    @SuppressWarnings("unchecked")
    public static <T> T lazyInit(Class<?> type, Supplier<T> initializer) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                type.getInterfaces(),
                new LazyInitInstance(initializer)
        );
    }


    /**
     * 如果入参对象是被代理后的对象，而且代理器是 LazyInitInstance，并且未经过初始化，则返回 true，否则返回 false。
     */
    public static boolean isDelegatedAndNotInited(Object object) {
        if (!Proxy.isProxyClass(object.getClass())) {
            return false;
        }

        InvocationHandler handler = Proxy.getInvocationHandler(object);
        if (!(handler instanceof LazyInitInstance)) {
            return false;
        }

        return !((LazyInitInstance) handler).isInitialized();
    }


    /**
     * 此懒加载实例中，被代理的实例是否加载过
     */
    public boolean isInitialized() {
        return status.get() == S_AVAILABLE;
    }


    /**
     * 调用被代理对象的方法，如果被代理不存在，会调用初始化器，生成对应对象。
     * @param proxy JDK 生成的代理对象，不是通过初始化器得到的对象。
     * @param method 被调用的方法。
     * @param args 方法调用参数
     * @return 返回真实值。
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (delegated == null) {
            initialize();
        }

        return method.invoke(this.delegated, args);
    }


    /**
     * lazy-init 真实被代理的对象
     */
    private void initialize() {
        while (this.status.get() != S_AVAILABLE) {
            if (this.status.get() == S_INITIALIZING) {
                Thread.yield();
                continue;
            }

            if (this.status.compareAndSet(S_UNAVAILABLE, S_INITIALIZING)) {
                try {
                    this.delegated = this.initializer.get();
                    this.status.set(S_AVAILABLE);
                    return;
                } catch (Exception e) {
                    this.status.set(S_UNAVAILABLE);
                    throw e;
                }
            }
        }
    }

}
