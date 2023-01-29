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


package org.limbo.flowjob.common.utils.concurrent;

import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Brozen
 * @since 2022-12-21
 */
public class Lockable<T> {

    /**
     * 读写锁
     */
    private final ReentrantReadWriteLock lock;

    /**
     * 加锁访问的对象
     */
    private volatile T locked;

    public Lockable() {
        this(null);
    }


    public Lockable(T locked) {
        this.lock = new ReentrantReadWriteLock();
        this.locked = locked;
    }


    /**
     * 设置可锁变量的值，申请写锁
     */
    public void set(T object) {
        ReentrantReadWriteLock.WriteLock writeLock = writeLock();
        writeLock.lock();
        try {
            this.locked = object;
        } finally {
            writeLock.unlock();
        }
    }


    /**
     * 读取可锁变量的值，直接返回，无锁。
     */
    public T get() {
        return locked;
    }


    /**
     * 返回写锁
     */
    protected ReentrantReadWriteLock.WriteLock writeLock() {
        return lock.writeLock();
    }


    /**
     * 返回读锁
     */
    protected ReentrantReadWriteLock.ReadLock readLock() {
        return lock.readLock();
    }


    /**
     * 申请锁后执行操作
     * @param operation 操作
     * @param mode 加锁模式
     * @see Mode
     */
    public void run(Consumer<T> operation, Mode mode) {
        switch (mode) {
            case READ:
                runInReadLock(operation);
                break;

            case WRITE:
                runInWriteLock(operation);

            default:
                throw new IllegalArgumentException("未知的加锁模式：" + mode);
        }
    }


    /**
     * 在申请写锁后执行操作
     */
    protected void runInWriteLock(Consumer<T> operation) {
        Objects.requireNonNull(operation, "operation");
        ReentrantReadWriteLock.WriteLock writeLock = writeLock();
        writeLock.lock();
        try {
            operation.accept(locked);
        } finally {
            writeLock.unlock();
        }
    }


    /**
     * 在申请读锁后执行操作
     */
    protected void runInReadLock(Consumer<T> operation) {
        Objects.requireNonNull(operation, "operation");
        ReentrantReadWriteLock.ReadLock readLock = readLock();
        readLock.lock();
        try {
            operation.accept(locked);
        } finally {
            readLock.unlock();
        }
    }


    /**
     * 在申请锁后执行操作，并返回操作的结果
     * @param operation 操作
     * @param mode 加锁模式
     * @param <R> 操作返回结果类型
     * @return 操作返回结果
     */
    public <R> R invoke(Function<T, R> operation, Mode mode) {
        switch (mode) {
            case READ:
                return invokeInReadLock(operation);

            case WRITE:
                return invokeInWriteLock(operation);

            default:
                throw new IllegalArgumentException("未知的加锁模式：" + mode);
        }
    }


    /**
     * 在申请写锁后执行操作，并返回
     */
    protected <R> R invokeInWriteLock(Function<T, R> operation) {
        Objects.requireNonNull(operation, "operation");
        ReentrantReadWriteLock.WriteLock writeLock = writeLock();
        writeLock.lock();
        try {
            return operation.apply(locked);
        } finally {
            writeLock.unlock();
        }
    }


    /**
     * 在申请读锁后执行操作，并返回
     */
    protected <R> R invokeInReadLock(Function<T, R> operation) {
        Objects.requireNonNull(operation, "operation");
        ReentrantReadWriteLock.ReadLock readLock = readLock();
        readLock.lock();
        try {
            return operation.apply(locked);
        } finally {
            readLock.unlock();
        }
    }


    /**
     * 加锁模式，支持读锁、写锁
     */
    enum Mode {
        /**
         * 读锁
         */
        READ,

        /**
         * 写锁
         */
        WRITE
    }

}
