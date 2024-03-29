/*
  betahouse.us
  CopyRight (c) 2012 - 2019
 */
package us.betahouse.haetae.activity.model.common;

import org.springframework.data.domain.Page;
import us.betahouse.haetae.activity.dal.model.YouthLearningDO;
import us.betahouse.haetae.activity.model.basic.YouthLearningBO;
import us.betahouse.util.utils.PageUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author MessiahJK
 * @version : PageList.java 2019/01/16 10:51 MessiahJK
 */
public class PageList<E> {

    /**
     * 总数据
     */
    private Long totalElements;

    /**
     * 总页码
     */
    private Integer totalPages;

    /**
     * 每页大小
     */
    private Integer size;

    /**
     * 当前页
     */
    private Integer number;

    /**
     * 当前页条数
     */
    private Integer numberOfElements;

    /**
     * 是否是首页
     */
    private Boolean first;

    /**
     * 是否是尾页
     */
    private Boolean end;

    /**
     * 数据
     */
    private List<E> content;

    @SuppressWarnings("unchecked")
    @Deprecated
    private void init(Page<?> page, Function convert) {
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.size = page.getSize();
        this.number = page.getNumber();
        this.numberOfElements = page.getNumberOfElements();
        this.first = page.isFirst();
        this.end = page.isLast();
        this.content = (List<E>) toStream(page.getContent())
                .filter(Objects::nonNull)
                .map(convert)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private void init(Page<?> page) {
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.size = page.getSize();
        this.number = page.getNumber();
        this.numberOfElements = page.getNumberOfElements();
        this.first = page.isFirst();
        this.end = page.isLast();
        this.content = (List<E>) page.getContent();
    }

    @SuppressWarnings("unchecked")
    private void init(PageList pageList, Function convert) {
        this.totalElements = pageList.getTotalElements();
        this.totalPages = pageList.getTotalPages();
        this.size = pageList.getSize();
        this.number = pageList.getNumber();
        this.numberOfElements = pageList.getNumberOfElements();
        this.first = pageList.isFirst();
        this.end = pageList.isEnd();
        this.content = (List<E>) toStream(pageList.getContent())
                .filter(Objects::nonNull)
                .map(convert)
                .collect(Collectors.toList());
    }

    private void init(PageUtil pageUtil){
        this.totalElements=pageUtil.getTotalElements().longValue();
        this.totalPages=pageUtil.getTotalPages();
        this.size=pageUtil.getSize();
        this.number=pageUtil.getNumber();
        this.numberOfElements=pageUtil.getNumberOfElements();
        this.first=pageUtil.getFirst();
        this.end=pageUtil.getEnd();
        this.content=pageUtil.getData();
    }

    public void init(List<E> content,Long totalElements,int number,int size) {
        if (content == null) {
            this.content = Collections.emptyList();
        }
        if(number < 0 || size < 0 ){
            throw new IllegalArgumentException("参数错误");
        }
        this.totalElements=totalElements;
        this.content=content;
        this.size=size;
        this.number=number;
        this.numberOfElements=content.size();
        this.totalPages=(int)((this.totalElements + size - 1)/size);
        if(number<=1){
            this.number=1;
            this.first=true;
        }else {
            this.first=false;
        }
        if(number>=totalPages){
            this.number=totalPages;
            this.end=true;
        }else {
            this.end=false;
        }
    }

    /**
     * content<T> ->PageList<T>
     *
     */
    public PageList(List<E> content,Long totalElements,int number,int size){
        this.init(content, totalElements, number, size);
    }

    /**
     * PageUtil<T> ->PageList<T>
     *
     * @param pageUtil  util/src/main/java/us/betahouse/util/utils/PageUtil.java
     */
    public PageList(PageUtil<?> pageUtil){
        this.init(pageUtil);
    }

    /**
     * 该方法以被优化，在新版本不建议使用
     *
     * 优化详情请见 {@link #PageList(Function,Page)}
     * @param page    org.springframework.data.domain.page jpa分页类
     * @param convert 类型转换函数
     */
    @Deprecated
    public PageList(Page<?> page, Function convert) {
        this.init(page, convert);
    }

    /**
     * Page<T> ->PageList<T>
     *
     * @param page org.springframework.data.domain.page jpa分页类
     */
    public PageList(Page<?> page) {
        this.init(page);
    }

    /**
     * PageList<V> ->PageList<T>
     *
     * @param pageList
     * @param convert
     */
    public PageList(PageList pageList, Function convert) {
        this.init(pageList, convert);
    }


    public Long getTotalElements() {
        return totalElements;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public Integer getSize() {
        return size;
    }

    public Integer getNumber() {
        return number;
    }

    public Integer getNumberOfElements() {
        return numberOfElements;
    }

    public Boolean isFirst() {
        return first;
    }

    public Boolean isEnd() {
        return end;
    }

    public List<E> getContent() {
        return content;
    }

    public void setContent(List<E> content){this.content=content;}


    /**
     * 转换stream 如果为空 返回空流 避免npe
     *
     * @param collection 集合
     * @param <T>        类型
     * @return 流
     */
    public static <T> Stream<T> toStream(Collection<T> collection) {
        if (collection == null || collection.isEmpty()) {
            return Stream.empty();
        }
        return collection.stream();
    }


    /**
     * 对带function的init的优化
     *
     * @author xxj
     * @param page
     * @param convert
     * @param <T>
     */
    public <T> void superInit(Page<T> page, Function<T,E> convert){
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.size = page.getSize();
        this.number = page.getNumber();
        this.numberOfElements = page.getNumberOfElements();
        this.first = page.isFirst();
        this.end = page.isLast();
        this.content =toStream(page.getContent())
                .filter(Objects::nonNull)
                .map(convert)
                .collect(Collectors.toList());
    }

    /**
     * 此后建议使用这个构造函数进行带转换的初始化
     *
     * @param function
     * @param page
     * @param <T> page中内涵的原始数据类型
     */
    public <T> PageList(Function<T,E> function,Page<T> page){
        superInit(page,function);
    }
}
