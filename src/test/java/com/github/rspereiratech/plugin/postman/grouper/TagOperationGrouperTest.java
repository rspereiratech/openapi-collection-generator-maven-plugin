package com.github.rspereiratech.plugin.postman.grouper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.rspereiratech.plugin.core.callback.CallbackProcessor;
import com.github.rspereiratech.plugin.postman.builder.ItemBuilder;
import com.github.rspereiratech.plugin.postman.model.PostmanItem;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.callbacks.Callback;

class TagOperationGrouperTest {

    private final ItemBuilder ib = mock(ItemBuilder.class);
    private final CallbackProcessor cp = mock(CallbackProcessor.class);
    private final TagOperationGrouper g = new TagOperationGrouper(ib, cp);

    @Test
    void group_groupsByFirstTag_andUsesDefault_whenNoTag() {
        Operation tagged = new Operation().tags(List.of("Pets"));
        Operation untagged = new Operation();
        OpenAPI api = new OpenAPI().paths(new Paths());
        api.getPaths().addPathItem("/a", new PathItem().get(tagged));
        api.getPaths().addPathItem("/b", new PathItem().post(untagged));
        when(ib.build(anyString(), anyString(), any(), anyString(), any()))
                .thenReturn(PostmanItem.request("R", null));

        Map<String, List<PostmanItem>> r = g.group(api, "b");
        assertEquals(1, r.get("Pets").size());
        assertEquals(1, r.get("default").size());
    }

    @Test
    void group_putsCallbacksInSeparateFolder() {
        Operation op = new Operation().summary("S");
        Callback cb = new Callback();
        cb.addPathItem("{$req}", new PathItem().post(new Operation()));
        op.setCallbacks(Map.of("evt", cb));
        OpenAPI api = new OpenAPI().paths(new Paths());
        api.getPaths().addPathItem("/p", new PathItem().get(op));

        when(ib.build(anyString(), anyString(), any(), anyString(), any()))
                .thenReturn(PostmanItem.request("R", null));
        when(cp.extractCallbackPaths(any(), anyString(), any()))
                .thenReturn(Map.of("/callbacks/x", new PathItem().post(new Operation())));

        Map<String, List<PostmanItem>> r = g.group(api, "b");
        assertTrue(r.containsKey("Callbacks"));
        verify(cp).extractCallbackPaths(any(), anyString(), any());
    }

    @Test
    void group_doesNotInvokeCallbackProcessor_whenNoCallbacks() {
        Operation op = new Operation();
        OpenAPI api = new OpenAPI().paths(new Paths());
        api.getPaths().addPathItem("/p", new PathItem().get(op));
        when(ib.build(anyString(), anyString(), any(), anyString(), any()))
                .thenReturn(PostmanItem.request("R", null));

        Map<String, List<PostmanItem>> r = g.group(api, "b");
        assertEquals(1, r.size());
        assertTrue(r.containsKey("default"));
    }
}
