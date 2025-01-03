
/*
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 */

package org.apache.ofbiz.solr.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.ServiceUtil;
import org.apache.ofbiz.service.testtools.OFBizTestCase;

public class SolrTests extends OFBizTestCase {

    protected GenericValue userLogin = null;
    private Map<String, Object> context;
    private Map<String, Object> response;
    private String validTestProductId = "GZ-1006";
    private String validTestProductId_2 = "GZ-1005";
    private String invalidTestProductId = validTestProductId + validTestProductId;

    public SolrTests(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        userLogin = EntityQuery.use(delegator).from("UserLogin").where("userLoginId", "system").queryOne();
    }

    @Override
    protected void tearDown() throws Exception {
    }

    public void testAddProductToIndex() throws Exception {

        GenericValue product = EntityQuery.use(delegator).from("Product").where("productId", validTestProductId).queryOne();

        Map<String, Object> ctx = new HashMap<String, Object>();
        ctx.put("instance", product);

        Map<String, Object> resp = dispatcher.runSync("addToSolr", ctx);
        if (ServiceUtil.isError(resp)) {
            String errorMessage = ServiceUtil.getErrorMessage(resp);
            throw new Exception(errorMessage);
        }
        assertTrue("Could not init search index", ServiceUtil.isSuccess(resp));

        Map<String, Object> sctx = new HashMap<String, Object>();
        sctx.put("productCategoryId", "102");

        Map<String, Object> sresp = dispatcher.runSync("solrProductsSearch", sctx);
        if (ServiceUtil.isError(sresp)) {
            String errorMessage = ServiceUtil.getErrorMessage(sresp);
            throw new Exception(errorMessage);
        }
        assertTrue("Could not query search index", ServiceUtil.isSuccess(sresp));
    }

    /**
     * Test add to solr index.
     * @throws Exception the exception
     */
    public void testAddToSolrIndex() throws Exception {
        System.setProperty("SolrDispatchFilter", "runsAfterControlFilter");
        context = new HashMap<>();
        context.put("productId", validTestProductId);
        response = dispatcher.runSync("addToSolrIndex", context);
        if (ServiceUtil.isError(response)) {
            String errorMessage = ServiceUtil.getErrorMessage(response);
            throw new Exception(errorMessage);
        }
        assertTrue("Could not add Product to Index", ServiceUtil.isSuccess(
                response));
        System.clearProperty("SolrDispatchFilter");
    }

    /**
     * Test add to solr index invalid product.
     * @throws Exception the exception
     */
    public void testAddToSolrIndexInvalidProduct() throws Exception {
        System.setProperty("SolrDispatchFilter", "runsAfterControlFilter");
        context = new HashMap<>();
        context.put("productId", invalidTestProductId);
        response = dispatcher.runSync("addToSolrIndex", context);
        if (ServiceUtil.isError(response)) {
            String errorMessage = ServiceUtil.getErrorMessage(response);
            throw new Exception(errorMessage);
        }
        assertTrue("Could not test the addition of an invalid product to the Solr index", ServiceUtil.isSuccess(
                response));
        System.clearProperty("SolrDispatchFilter");
    }

    public void testAddListToSolrIndex() throws Exception {
        System.setProperty("SolrDispatchFilter", "runsAfterControlFilter");
        List<Map<String, Object>> products = new ArrayList<>();
        Map<String, Object> product_1 = new HashMap<>();
        Map<String, Object> product_2 = new HashMap<>();
        GenericValue validTestProduct = EntityQuery.use(delegator).from("Product").where("productId", validTestProductId).queryOne();
        GenericValue validTestProduct_2 = EntityQuery.use(delegator).from("Product").where("productId", validTestProductId_2).queryOne();

        product_1.put("productId", validTestProduct);
        product_2.put("productId", validTestProduct_2);

        products.add(product_1);
        products.add(product_2);
        context  = new HashMap<>();
        context.put("fieldList", products);

        response = dispatcher.runSync("addListToSolrIndex", context);
        if (ServiceUtil.isError(response)) {
            String errorMessage = ServiceUtil.getErrorMessage(response);
            throw new Exception(errorMessage);
        }
        assertTrue("Could not add products to index", ServiceUtil.isSuccess(response));
        System.clearProperty("SolrDispatchFilter");
    }

    /**
     * Test add list to solr index invalid products.
     * @throws Exception the exception
     */
    public void testAddListToSolrIndexInvalidProducts() throws Exception {
        System.setProperty("SolrDispatchFilter", "runsAfterControlFilter");
        List<Map<String, Object>> products = new ArrayList<>();
        Map<String, Object> product_1 = new HashMap<>();
        Map<String, Object> product_2 = new HashMap<>();
        GenericValue testProduct = EntityQuery.use(delegator).from("Product").where("productId", validTestProductId).queryOne();
        GenericValue testProduct_2 = EntityQuery.use(delegator).from("Product").where("productId", validTestProductId_2).queryOne();

        testProduct.replace("productId", invalidTestProductId);
        testProduct.replace("productId", invalidTestProductId);

        product_1.put("productId", testProduct);
        product_2.put("productId", testProduct_2);

        products.add(product_1);
        products.add(product_2);
        context  = new HashMap<>();
        context.put("fieldList", products);

        response = dispatcher.runSync("addListToSolrIndex", context);
        if (ServiceUtil.isError(response)) {
            String errorMessage = ServiceUtil.getErrorMessage(response);
            throw new Exception(errorMessage);
        }
        assertTrue("Could not test adding invalid products to index", ServiceUtil.isSuccess(response));
        System.clearProperty("SolrDispatchFilter");
    }
}

