package us.betahouse.haetae.model.user.request;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import us.betahouse.haetae.common.RestRequest;

public class UserFeedBackRestRequest extends RestRequest {

    /**
     * 每页大小
     */
    private Integer size;

    /**
     * 页数
     */
    private Integer page;

    /**
     * 反馈id
     */
    private String feedBackId;

    /**
     * 反馈标题，回评的话为null
     */
    private String title;

    /**
     * 反馈内容
     */
    private String context;

    /**
     * 反馈的用户信息
     */
    private String userId;

    /**
     * 下一个评论的id(暂定？)
     */
    private String feedBackNext;

    /**
     * 反馈的头id(暂定？)
     */
    private String feedBackHead;

    /**
     * 反馈时所用的版本
     */
    private String version;

    /**
     * 目标id
     */
    private String targetId;

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getFeedBackId() {
        return feedBackId;
    }

    public void setFeedBackId(String feedBackId) {
        this.feedBackId = feedBackId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFeedBackNext() {
        return feedBackNext;
    }

    public void setFeedBackNext(String feedBackNext) {
        this.feedBackNext = feedBackNext;
    }

    public String getFeedBackHead() {
        return feedBackHead;
    }

    public void setFeedBackHead(String feedBackHead) {
        this.feedBackHead = feedBackHead;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public Pageable getPageable(){
        if(page==null||page<0){
            page=0;
        }
        if(size==null||size<0){
            size=10;
        }
        return PageRequest.of(page,size);
    }
}
