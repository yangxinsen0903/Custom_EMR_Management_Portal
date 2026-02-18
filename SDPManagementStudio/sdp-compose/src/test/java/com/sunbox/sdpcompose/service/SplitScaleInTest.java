package com.sunbox.sdpcompose.service;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class SplitScaleInTest {

    @Test
    public void testSplit(){
        ConfScalingTask task = new ConfScalingTask();
        task.setClusterId("TestCluster01");
        task.setVmRole("CORE");
        task.setScalingCount(147);
        task.setBeforeScalingCount(150);
        task.setAfterScalingCount(task.getBeforeScalingCount() - task.getScalingCount());
        List<InfoClusterVm> infoClusterVmList = new ArrayList<>();
        for(int index = 0; index < task.getBeforeScalingCount(); index++){
            infoClusterVmList.add(new InfoClusterVm("vm" + index));
        }

        boolean descendingSplit = false;
        int taskCountPreTask = 0;
        if (task.getVmRole().equalsIgnoreCase("CORE")) {
            descendingSplit = true;
            if (isHbaseCluster(task.getClusterId())) {
                taskCountPreTask = 1;
            } else {
                taskCountPreTask = 5;
            }
        } else if (task.getVmRole().equalsIgnoreCase("TASK")) {
            taskCountPreTask = 50;
        }

        int beforeScalingCount = task.getBeforeScalingCount();
        int fromSubListIndex = 0;
        int toSubListIndex = 0;
        while(beforeScalingCount > task.getAfterScalingCount()) {
            if(descendingSplit) {
                int oneTenthCount = (int) (beforeScalingCount / 10);
                if (oneTenthCount < 1) {
                    oneTenthCount = 1;
                }

                taskCountPreTask = oneTenthCount;
            }

            int afterScalingCount = beforeScalingCount - taskCountPreTask;
            if (afterScalingCount < task.getAfterScalingCount()) {
                afterScalingCount = task.getAfterScalingCount();
            }

            if (beforeScalingCount == afterScalingCount) {
                continue;
            }

            toSubListIndex += taskCountPreTask;
            if (fromSubListIndex >= infoClusterVmList.size()) {
                continue;
            }

            if (toSubListIndex >= infoClusterVmList.size()) {
                toSubListIndex = infoClusterVmList.size();
            }

            if (beforeScalingCount - afterScalingCount < toSubListIndex - fromSubListIndex) {
                toSubListIndex -= ((toSubListIndex - fromSubListIndex) - (beforeScalingCount - afterScalingCount));
            }

            List<InfoClusterVm> subInfoClusterVms = infoClusterVmList.subList(fromSubListIndex, toSubListIndex);

            List<String> vmnames = new ArrayList<>();
            subInfoClusterVms.stream().forEach(vm -> {
                vmnames.add(vm.getVmName());
                System.out.print(vm.getVmName() + ", ");
            });
            System.out.println("|");
//            getLogger().info("infoClusterVmList subList taskCountPreTask:{},from:{},to:{},size:{},vmnames:{}",
//                    taskCountPreTask,
//                    fromSubListIndex,
//                    toSubListIndex,
//                    infoClusterVmList.size(),
//                    vmnames);

            int actualScalingCount = beforeScalingCount - afterScalingCount;
            if (actualScalingCount > vmnames.size()) {
                actualScalingCount = vmnames.size();
            }

            System.out.println(String.format("beforeScalingCount:%d, setAfterScalingCount:%d, setExpectCount:%d, setScalingCount:%d",
                    beforeScalingCount,
                    beforeScalingCount - actualScalingCount,
                    beforeScalingCount - actualScalingCount,
                    actualScalingCount));
            ConfScalingTask partTask = new ConfScalingTask();
            BeanUtils.copyProperties(task, partTask);
            partTask.setTaskId(UUID.randomUUID().toString());
            partTask.setBeforeScalingCount(beforeScalingCount);
//            partTask.setAfterScalingCount(beforeScalingCount - actualScalingCount);
//            partTask.setExpectCount(partTask.getAfterScalingCount());
//            partTask.setScalingCount(actualScalingCount);
//            partTask.setCreateTime(DateUtils.addMilliseconds(new Date(), indexOfTaskNode * 100));
//            partTask.setState(ConfScalingTask.SCALINGTASK_Create);

            // region 保存数据
//            taskMapper.insert(partTask);
//            getLogger().info("insert part task:{}", partTask);

            // endregion

            // region 更新infoclustervm表，写入scaleintaskid

//            infoClusterVmMapper.updateScaleinTaskIdByClusterIdAndVmNames(
//                    confCluster.getClusterId(),
//                    vmnames,
//                    partTask.getTaskId());

            // endregion

//            addTaskWaitQueue(confCluster, InfoClusterOperationPlan.Plan_OP_ScaleIn, partTask);

            beforeScalingCount -= taskCountPreTask;
            fromSubListIndex += taskCountPreTask;
        }
        System.out.println("ok");
    }

    private boolean isHbaseCluster(String clusterId) {
        return false;
    }

    private static class InfoClusterVm{
        private String vmName;

        public InfoClusterVm(String vmName) {
            this.vmName = vmName;
        }

        public String getVmName() {
                return this.vmName;
        }
    }

    private static class ConfScalingTask{
        private String clusterId;
        private String vmRole;
        private String groupName;
        private Integer scalingCount;
        private Integer beforeScalingCount;
        private Integer afterScalingCount;

        public String getClusterId() {
            return clusterId;
        }

        public void setClusterId(String clusterId) {
            this.clusterId = clusterId;
        }

        public String getVmRole() {
            return vmRole;
        }

        public void setVmRole(String vmRole) {
            this.vmRole = vmRole;
        }


        public int getScalingCount() {
            return this.scalingCount;
        }

        public int getBeforeScalingCount() {
            return this.beforeScalingCount;
        }

        public int getAfterScalingCount() {
            return this.afterScalingCount;
        }

        public void setAfterScalingCount(Integer afterScalingCount) {
            this.afterScalingCount = afterScalingCount;
        }

        public void setTaskId(String string) {

        }

        public void setBeforeScalingCount(int beforeScalingCount) {
            this.beforeScalingCount = beforeScalingCount;
        }

        public void setScalingCount(int scalingCount) {
            this.scalingCount = scalingCount;
        }
    }
}
