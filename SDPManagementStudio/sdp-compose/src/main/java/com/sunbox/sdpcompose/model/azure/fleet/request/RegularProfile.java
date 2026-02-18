package com.sunbox.sdpcompose.model.azure.fleet.request;

/**
 * on-demand profile
 */
public class RegularProfile {
    private String allocationStrategy;

    private Integer capacity;
    private Integer minCapacity;

    public String getAllocationStrategy() {
        return allocationStrategy;
    }

    public void setAllocationStrategy(String allocationStrategy) {
        this.allocationStrategy = allocationStrategy;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public Integer getMinCapacity() {
        return minCapacity;
    }

    public void setMinCapacity(Integer minCapacity) {
        this.minCapacity = minCapacity;
    }

    /**
     *  分配策略枚举
     */
    public enum RegularAllocationStrategyEnum{

        /**
         * 1. 按最低价格，优先选择价格低的
         */
        LowestPrice("LowestPrice","价格优先"),
        /**
         * 1. 给不同sku指定优先级，按照优先级分配，例如有RI或者saving plan的SKU可以优先级设置最高
         * 2. 按照优先级逐个创建vm，例如一共需要15个，第一优先级可以满足10个，那么第二优先级满足剩余的5个
         */
        Prioritized("Prioritized","按照指定的优先级");

        private String code;
        private String desc;

        RegularAllocationStrategyEnum(String code,String desc){
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    @Override
    public String toString() {
        return "RegularProfile{" +
                "allocationStrategy='" + allocationStrategy + '\'' +
                ", capacity=" + capacity +
                ", minCapacity=" + minCapacity +
                '}';
    }
}
