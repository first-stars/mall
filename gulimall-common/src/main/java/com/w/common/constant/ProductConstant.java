package com.w.common.constant;

/**
 * @author xin
 * @date 2023-01-10-11:38
 */
public class ProductConstant {
    public enum AttrEnum{
        ATTR_TYPE_BASE(1,"基本属性"), ATTR_TYPE_SALE(2,"销售属性");
        private int code;
        private String msg;

        AttrEnum(int code,String msg){
            this.code=code;
            this.msg=msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }

    public enum StatusEnum{
        NEW_SPU(0,"新建"), NEW_UP(1,"商品上架"),NEW_DOWN(2,"商品下架");
        private int code;
        private String msg;

        StatusEnum(int code,String msg){
            this.code=code;
            this.msg=msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }
}
