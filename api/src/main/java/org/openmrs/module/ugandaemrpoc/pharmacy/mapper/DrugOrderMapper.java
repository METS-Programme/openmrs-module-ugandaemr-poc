package org.openmrs.module.ugandaemrpoc.pharmacy.mapper;

import org.openmrs.module.ugandaemrpoc.api.lab.mapper.OrderMapper;

import java.io.Serializable;

public class DrugOrderMapper extends OrderMapper implements Serializable {
    private Double dose;

    private String doseUnits;

    private String frequency;

    private Boolean asNeeded = false;

    private Double quantity;

    private String quantityUnits;

    private String drug;

    private String asNeededCondition;

    private Integer numRefills;

    private String dosingInstructions;

    private Integer duration;

    private String durationUnits;

    private String route;

    private String brandName;

    private Boolean dispenseAsWritten = Boolean.FALSE;

    private String drugNonCoded;

    private String strength;

    public Double getDose() {
        return dose;
    }

    public void setDose(Double dose) {
        this.dose = dose;
    }

    public String getDoseUnits() {
        return doseUnits;
    }

    public void setDoseUnits(String doseUnits) {
        this.doseUnits = doseUnits;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public Boolean getAsNeeded() {
        return asNeeded;
    }

    public void setAsNeeded(Boolean asNeeded) {
        this.asNeeded = asNeeded;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public String getQuantityUnits() {
        return quantityUnits;
    }

    public void setQuantityUnits(String quantityUnits) {
        this.quantityUnits = quantityUnits;
    }

    public String getDrug() {
        return drug;
    }

    public void setDrug(String drug) {
        this.drug = drug;
    }

    public String getAsNeededCondition() {
        return asNeededCondition;
    }

    public void setAsNeededCondition(String asNeededCondition) {
        this.asNeededCondition = asNeededCondition;
    }

    public Integer getNumRefills() {
        return numRefills;
    }

    public void setNumRefills(Integer numRefills) {
        this.numRefills = numRefills;
    }

    public String getDosingInstructions() {
        return dosingInstructions;
    }

    public void setDosingInstructions(String dosingInstructions) {
        this.dosingInstructions = dosingInstructions;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getDurationUnits() {
        return durationUnits;
    }

    public void setDurationUnits(String durationUnits) {
        this.durationUnits = durationUnits;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public Boolean getDispenseAsWritten() {
        return dispenseAsWritten;
    }

    public void setDispenseAsWritten(Boolean dispenseAsWritten) {
        this.dispenseAsWritten = dispenseAsWritten;
    }

    public String getDrugNonCoded() {
        return drugNonCoded;
    }

    public void setDrugNonCoded(String drugNonCoded) {
        this.drugNonCoded = drugNonCoded;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }
}
