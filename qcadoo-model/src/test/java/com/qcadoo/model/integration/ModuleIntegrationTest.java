/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 0.4.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

package com.qcadoo.model.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Map;

import org.junit.Test;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.PluginManager;

public class ModuleIntegrationTest extends IntegrationTest {

    @Test
    public void shouldHaveAdditinanalModelsFieldsAndHooks() throws Exception {
        // given
        DataDefinition productDao = dataDefinitionService.get(PLUGIN_PRODUCTS_NAME, ENTITY_NAME_PRODUCT);
        DataDefinition machineDao = dataDefinitionService.get(PLUGIN_MACHINES_NAME, ENTITY_NAME_MACHINE);
        DataDefinition componentDao = dataDefinitionService.get(PLUGIN_PRODUCTS_NAME, ENTITY_NAME_COMPONENT);

        Entity machine = machineDao.save(createMachine("asd"));

        Entity product = createProduct("asd", "asd");
        product.setField("changeableName", "xxx");
        product = productDao.save(product);

        Entity component = createComponent("name", product, machine);
        component.setField("machineName", "test");

        // when
        component = componentDao.save(component);

        // then

        assertEquals("test", component.getField("machineName"));
        assertEquals("XXX", product.getField("changeableName"));
        assertNotNull(component.getField("machine"));

        Map<String, Object> componentResult = jdbcTemplate.queryForMap("select * from " + TABLE_NAME_COMPONENT);

        assertNotNull(componentResult);
        assertEquals("test", componentResult.get("machineName"));

        Map<String, Object> productResult = jdbcTemplate.queryForMap("select * from " + TABLE_NAME_PRODUCT);

        assertNotNull(productResult);
        assertEquals("XXX", productResult.get("changeableName"));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotHaveAdditinanalModels() throws Exception {
        // given
        applicationContext.getBean(PluginManager.class).disablePlugin("machines");

        DataDefinition machineDao = dataDefinitionService.get(PLUGIN_MACHINES_NAME, ENTITY_NAME_MACHINE);

        // when
        machineDao.save(createMachine("asd"));
    }

    @Test
    public void shouldNotHaveAdditinanalFieldsAndHooks() throws Exception {
        // given
        DataDefinition machineDao = dataDefinitionService.get(PLUGIN_MACHINES_NAME, ENTITY_NAME_MACHINE);
        Entity machine = machineDao.save(createMachine("asd"));

        applicationContext.getBean(PluginManager.class).disablePlugin("machines");

        DataDefinition productDao = dataDefinitionService.get(PLUGIN_PRODUCTS_NAME, ENTITY_NAME_PRODUCT);
        DataDefinition componentDao = dataDefinitionService.get(PLUGIN_PRODUCTS_NAME, ENTITY_NAME_COMPONENT);

        Entity product = createProduct("asd", "asd");
        product.setField("changeableName", "xxx");
        product = productDao.save(product);

        Entity component = createComponent("name", product, null);
        component.setField("machineName", "test");

        // when
        component = componentDao.save(component);

        // then

        assertEquals("xxx", product.getField("changeableName"));
        assertNull(component.getField("machineName"));
        assertNull(component.getField("machines"));

        Map<String, Object> componentResult = jdbcTemplate.queryForMap("select * from " + TABLE_NAME_COMPONENT);

        assertNotNull(componentResult);
        assertNull(componentResult.get("machineName"));

        Map<String, Object> productResult = jdbcTemplate.queryForMap("select * from " + TABLE_NAME_PRODUCT);

        assertNotNull(productResult);
        assertEquals("xxx", productResult.get("changeableName"));
    }

}