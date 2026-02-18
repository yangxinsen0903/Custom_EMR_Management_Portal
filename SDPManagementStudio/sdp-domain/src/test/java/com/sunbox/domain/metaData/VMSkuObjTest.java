package com.sunbox.domain.metaData;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author wangda
 * @date 2024/12/17
 */
class VMSkuObjTest {

    @Test
    public void parse_base() {
        String skuName = "Standard_E32-16s_v5";
        VMSkuObj obj = new VMSkuObj();
        obj.parse(skuName);
        assertEquals("E", obj.getFamily());
        assertEquals(Integer.valueOf(32), obj.getVCpu());
        assertEquals("v5", obj.getVersion());
        assertTrue(obj.isIntelCPU());
    }

    @Test
    public void parse_base_singleCpuCore() {
        String skuName = "Standard_E32as_v5";
        VMSkuObj obj = new VMSkuObj();
        obj.parse(skuName);
        assertEquals("E", obj.getFamily());
        assertEquals(Integer.valueOf(32), obj.getVCpu());
        assertEquals("v5", obj.getVersion());
        assertTrue(obj.isAMDCpu());
    }

    @Test
    public void parse_base_GpuSku() {
        String skuName = "Standard_NC4as_T4_v3";
        VMSkuObj obj = new VMSkuObj();
        obj.parse(skuName);
        assertEquals("NC", obj.getFamily());
        assertEquals(Integer.valueOf(4), obj.getVCpu());
        assertEquals("v3", obj.getVersion());
        assertTrue(obj.isAMDCpu());
        assertTrue(obj.isSupportAccelerator());
        assertEquals("T4", obj.getAcceleratorType());
    }

    @Test
    public void parse_noVersion() {
        String skuName = "Basic_A0";
        VMSkuObj obj = new VMSkuObj();
        obj.parse(skuName);
        assertEquals("A", obj.getFamily());
        assertEquals(Integer.valueOf(0), obj.getVCpu());
        assertEquals("", obj.getVersion());
        assertFalse(obj.isAMDCpu());
        assertFalse(obj.isSupportAccelerator());
        assertTrue(obj.isIntelCPU());

    }
}