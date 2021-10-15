### 任务类型
1. CRON
2. Fixed rate 固定间隔
3. Second delay 支持1~60秒间隔的秒级延迟调度，即每次任务执行完成后，间隔秒级时间再次触发调度。 适用于对实时性要求比较高的业务，例如需要不停做轮询的准实时业务。
4. 固定延迟  -> 衍生出临时任务，只执行一次
5. DAG 工作流


Plan {
    Jobs: []
}

Plan -> PlanRecord -> PlanInstance(retry) 
-> n JobRecord -> JobInstance(retyr)
-> Task(to worker)
