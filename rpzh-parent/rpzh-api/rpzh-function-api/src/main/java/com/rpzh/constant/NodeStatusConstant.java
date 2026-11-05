package com.rpzh.constant;

/**
 * 节点（设备）状态常量，对应 tb_node.status。
 *
 * <p>系统原有取值：0 解垛 / 1 入垛 / 2 未使用 / 3 故障 / 4 运维中(RFID)。
 * 新增 5 异常：长时间未收到已入垛设备上报的数据，或设备多次发出异常数据。
 * 异常设备仍在垛内（stack_id / case_no 等入垛数据保持不变），只是状态被标记。</p>
 */
public final class NodeStatusConstant {

    /** 解垛：设备已出垛，不占用垛位 */
    public static final String UNBIND = "0";
    /** 已入垛：正常工作 */
    public static final String ENTERED = "1";
    /** 未使用 */
    public static final String UNUSED = "2";
    /** 故障 / 损坏 */
    public static final String FAULT = "3";
    /** 运维中（RFID 维修流程使用） */
    public static final String MAINTAINING = "4";
    /** 异常：长时间未收到数据，或多次发出异常数据 */
    public static final String ABNORMAL = "5";

    private NodeStatusConstant() {
    }

    public static boolean isAbnormal(String status) {
        return ABNORMAL.equals(status);
    }

    /** 是否仍占用垛位：正常入垛或异常，都不应出现在"可入垛"列表里 */
    public static boolean inStack(String status) {
        return ENTERED.equals(status) || ABNORMAL.equals(status);
    }

    /** 是否允许执行入垛：仅解垛 / 未使用 */
    public static boolean canEnter(String status) {
        return UNBIND.equals(status) || UNUSED.equals(status) || status == null || status.isEmpty();
    }

    /** 是否允许执行出垛：已入垛或异常（异常设备也需要能解垛） */
    public static boolean canUnbind(String status) {
        return ENTERED.equals(status) || ABNORMAL.equals(status);
    }
}
