package com.sunbox.sdpcompose.model.azure.fleet.request;

public class SpotProfile {

    private String allocationStrategy;

    private Integer capacity;

    private String evictionPolicy;

    private boolean maintain;

    private String maxPricePerVM;

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

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getEvictionPolicy() {
        return evictionPolicy;
    }

    public void setEvictionPolicy(String evictionPolicy) {
        this.evictionPolicy = evictionPolicy;
    }

    public boolean isMaintain() {
        return maintain;
    }

    public void setMaintain(boolean maintain) {
        this.maintain = maintain;
    }

    public String getMaxPricePerVM() {
        return maxPricePerVM;
    }

    public void setMaxPricePerVM(String maxPricePerVM) {
        this.maxPricePerVM = maxPricePerVM;
    }

    public Integer getMinCapacity() {
        return minCapacity;
    }

    public void setMinCapacity(Integer minCapacity) {
        this.minCapacity = minCapacity;
    }

    @Override
    public String toString() {
        return "SpotProfile{" +
                "allocationStrategy='" + allocationStrategy + '\'' +
                ", capacity=" + capacity +
                ", evictionPolicy='" + evictionPolicy + '\'' +
                ", maintain=" + maintain +
                ", maxPricePerVM='" + maxPricePerVM + '\'' +
                ", minCapacity=" + minCapacity +
                '}';
    }

    /**
     * 分配策略枚举
     */
    public enum SpotAllocationStrategyEnum {

        /**
         * 1. 优先满足capacity，其次满足price
         * 2. 如果capacity都满足的情况下，选价格低的
         * 3. 如果capacity都不满足的target的话，选择可以满足最多的sku，依次列举
         */
        PriceCapacityOptimized("PriceCapacityOptimized","均衡模式"),

        /**
         * 1. 满足capacity，不考虑price
         * 2. 如果有多个sku都满足的情况下，选择数据中心剩余capacity最多的sku，考虑到spot被驱逐补足的情况
         * 3. 如果多个都不满足的情况下，先选择能够满足最多的sku
         */
        CapacityOptimized("CapacityOptimized","Capacity优先"),

        /**
         * 1. 按最低价格，优先选择价格低的
         */
        LowestPrice("LowestPrice","");

        private String code;
        private String desc;

        SpotAllocationStrategyEnum(String code,String desc){
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
}
