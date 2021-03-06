package com.opentable.hobknob.client;

import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.responses.EtcdKeysResponse;

import java.util.HashMap;

public class FeatureToggleProvider
{
    private EtcdClient _etcdClient;
    private String _applicationDirectoryKey;

    public FeatureToggleProvider(EtcdClient etcdClient, String applicationName)
    {
        _etcdClient = etcdClient;
        _applicationDirectoryKey = "v1/toggles/" + applicationName;
    }

    public HashMap<String,Boolean> get() throws Exception
    {
        EtcdKeysResponse etcdKeysResponse = _etcdClient.getDir(_applicationDirectoryKey).recursive().send().get();

        HashMap<String,Boolean> hashMap = new HashMap<>();
        for(EtcdKeysResponse.EtcdNode featureNode : etcdKeysResponse.node.nodes)
        {
            if (featureNode.dir)
            {
                for(EtcdKeysResponse.EtcdNode toggleNode : featureNode.nodes)
                {
                    if (toggleNode.key.endsWith("@meta")) continue;
                    addToggleValueToMap(toggleNode, hashMap);
                }
            }
            else
            {
                addToggleValueToMap(featureNode, hashMap);
            }
        }
        return hashMap;
    }

    private void addToggleValueToMap(EtcdKeysResponse.EtcdNode node, HashMap<String,Boolean> hashMap){
        Boolean featureToggleValue = parseFeatureToggleValue(node.value);
        if (featureToggleValue != null)
        {
            hashMap.put(node.key, featureToggleValue);
        }
    }

    private static Boolean parseFeatureToggleValue(String value)
    {
        if (value == null) return null;

        switch (value.toLowerCase())
        {
            case "true":
                return true;
            case "false":
                return false;
            default:
                return null;
        }
    }
}
