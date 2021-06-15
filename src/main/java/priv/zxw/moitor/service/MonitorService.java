package priv.zxw.moitor.service;

public interface MonitorService {

    /**
     * 新增请求访问量
     *
     * @param ip    请求地址
     * @return  是否新增成功
     */
    boolean  incrementVisitorCount(String ip);

    /**
     * 获取快照json
     *
     * @return  快照json
     */
    String getSnapshotAsJson();

    /**
     * 获取总共访问次数
     *
     * @return  总共访问次数
     */
    Integer getTotalCount();
}
