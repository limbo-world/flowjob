package org.limbo.flowjob.tracker.dao.po;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Brozen
 * @since 2021-07-05
 */
@Data
public abstract class PO implements Serializable {

    private static final long serialVersionUID = 1797761151294059019L;

    /**
     * 记录创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 记录更新时间
     */
    private LocalDateTime updatedAt;

}
