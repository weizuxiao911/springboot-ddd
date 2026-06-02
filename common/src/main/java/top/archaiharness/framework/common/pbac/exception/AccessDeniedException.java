package top.archaiharness.framework.common.pbac.exception;

/**
 * 访问拒绝异常
 *
 * 当权限评估结果为拒绝时抛出此异常
 *
 * @author framework
 */
public class AccessDeniedException extends RuntimeException {

    private final String policyCode;

    /**
     * 构造访问拒绝异常
     *
     * @param policyCode 拒绝访问的策略编码
     */
    public AccessDeniedException(String policyCode) {
        super(String.format("Access denied by policy: %s", policyCode));
        this.policyCode = policyCode;
    }

    /**
     * 构造访问拒绝异常
     *
     * @param policyCode 拒绝访问的策略编码
     * @param message 详细消息
     */
    public AccessDeniedException(String policyCode, String message) {
        super(message);
        this.policyCode = policyCode;
    }

    /**
     * 构造访问拒绝异常
     *
     * @param policyCode 拒绝访问的策略编码
     * @param cause 原因
     */
    public AccessDeniedException(String policyCode, Throwable cause) {
        super(String.format("Access denied by policy: %s", policyCode), cause);
        this.policyCode = policyCode;
    }

    /**
     * 获取拒绝访问的策略编码
     *
     * @return 策略编码
     */
    public String getPolicyCode() {
        return policyCode;
    }
}
