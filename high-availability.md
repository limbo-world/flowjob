## 高可用性 High Available

### Job下发失败策略
tracker生成一个JobContext后，存入DB状态为INIT，但是因突发情况没有下发给worker（如tracker宕机），
或下发给worker失败（如网络波动、worker宕机），此时导致JobContext状态为INIT。此时如何处理？

没有下发给worker（tracker宕机） 处理策略：
根据不同模式，进行不同的处理
1. 主从模式：新的主节点选举产生后，切换为leader后，查询所有异常的JobContext，重新下发；
2. 集群模式：待定…… TODO

下发给worker失败 处理策略：
1. JobContext下发添加超时机制，发生超时则触发下发失败事件；
2. 触发下发失败实际后，可配置N次重试下发；
3. N次重试失败，触发配置的其他下发失败策略（邮件报警、短信通知等）；

### 主从模式
#### 时间调度策略
所有Job在主节点上调度，相当于单机调度，使用单机时间轮算法即可。

#### 从节点动态扩展
TODO 是否需要支持？

#### 主从状态同步
从节点启动后，向主节点拉取；
应用场景：1. 从节点挂掉后的启动；2. 从节点动态扩展。

#### 主节点选举
1. 基于第三方中间件，如zookeeper;
2. 自行实现Raft协议

### 集群模式
#### 集群模式调度策略
##### 基于共享数据
基于redis实现时间轮算法，Job共享在redis上，集群中的tracker想redis竞争数据。

优点：
- tracker动态扩容、tracker挂掉的情况处理比较简单，tracker主要负责与worker通信

存在问题：
- pull/push
  时间轮中任务触发时，有两种方式让tracker节点得知：
    1. tracker轮询redis；
    2. 利用redis的watch机制进行通知。
    
- redis的性能瓶颈
  不论是pull/push模式，都依赖于redis进行调度，此时redis的性能会制约能够并发调度的最大任务数量。
  

##### 基于锁竞争
tracker基于Netty时间轮实现单机调度，集群中的每个tracker都调度所有Job。
当多个tracker调度同一个Job时，通过锁竞争机制，保证只有一个tracker能够将Job调度到自身。

优点：
- tracker动态扩容、tracker挂掉的情况无需考虑，只要不是集群的节点全部挂掉就还是可用的

存在问题：
- 锁竞争激烈
  为减少锁竞争的发生，尽量让tracker调度Job时打乱顺序。
  
- 数据共享
  集群中每个节点都需调度全部Job，当新增Job时，如何在集群节点间同步新增的Job？
  1. 定时轮询数据库
  2. gossip协议传播
    
- 性能瓶颈
  每个节点都调度全部Job，当Job数量过多时，无法通过集群扩展来分摊压力。
  且此时锁竞争机制会是性能瓶颈（如果通过redis实现锁，则redis再次成为性能瓶颈）。

##### 基于数据partition
将Job数据分区，每个Job对应到一个分区ID，每个分区ID由一个tracker负责调度。tracker动态扩容时，自动调整tracker调度的分区ID。

优点：
- Job数据量大时，能够很好的分摊数据压力，通过动态扩容，基本没有性能上限

存在如下问题
- 需考虑极为复杂的动态扩容、意外宕机的情况
  需要自行实现partition调整算法。自行实现算法时，需考虑调整过程中，调度是否可用？会不会出现短时间内，同一分区被两个tracker调度的情况？

- 新增Job时，需对应分区的tracker能够得知有Job新增
  1. tracker轮询DB
  2. gossip传播
  
