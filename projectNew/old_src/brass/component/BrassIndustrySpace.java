package com.f14.brass.component;

import com.f14.brass.consts.BrassIndustryType;

import java.util.LinkedHashSet;
import java.util.Set;

public class BrassIndustrySpace implements Cloneable {
    public String id;
    public Set<BrassIndustryType> availableIndustryTypes = new LinkedHashSet<>();
    public BrassIndustryCard builtIndustry;
    public BrassLocation location;


    @Override
    protected BrassIndustrySpace clone() {
        BrassIndustrySpace res;
        try {
            res = (BrassIndustrySpace) super.clone();
            return res;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Set<BrassIndustryType> getAvailableIndustryTypes() {
        return availableIndustryTypes;
    }

    public void setAvailableIndustryTypes(Set<BrassIndustryType> availableIndustryTypes) {
        this.availableIndustryTypes = availableIndustryTypes;
    }

    public BrassIndustryCard getBuiltIndustry() {
        return builtIndustry;
    }

    public void setBuiltIndustry(BrassIndustryCard builtIndustry) {
        this.builtIndustry = builtIndustry;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BrassLocation getLocation() {
        return location;
    }

    public void setLocation(BrassLocation location) {
        this.location = location;
    }
}
