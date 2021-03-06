package com.blackviking.menorahfarms.Models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class SponsoredFarmModel {

    private String sponsorReturn;
    private String cycleEndDate;
    private String cycleStartDate;
    private String sponsorRefNumber;
    private String unitPrice;
    private String sponsoredUnits;
    private String sponsoredFarmType;
    private String sponsoredFarmRoi;
    private String sponsorshipDuration;
    private long startPoint;
    private long totalAmountPaid;
    private String farmId;
    private String status;

    public SponsoredFarmModel() {
    }

    public SponsoredFarmModel(String sponsorReturn, String cycleEndDate, String cycleStartDate, String sponsorRefNumber, String unitPrice, String sponsoredUnits, String sponsoredFarmType, String sponsoredFarmRoi, String sponsorshipDuration, long startPoint, long totalAmountPaid, String farmId, String status) {
        this.sponsorReturn = sponsorReturn;
        this.cycleEndDate = cycleEndDate;
        this.cycleStartDate = cycleStartDate;
        this.sponsorRefNumber = sponsorRefNumber;
        this.unitPrice = unitPrice;
        this.sponsoredUnits = sponsoredUnits;
        this.sponsoredFarmType = sponsoredFarmType;
        this.sponsoredFarmRoi = sponsoredFarmRoi;
        this.sponsorshipDuration = sponsorshipDuration;
        this.startPoint = startPoint;
        this.totalAmountPaid = totalAmountPaid;
        this.farmId = farmId;
        this.status = status;
    }

    public String getSponsorReturn() {
        return sponsorReturn;
    }

    public void setSponsorReturn(String sponsorReturn) {
        this.sponsorReturn = sponsorReturn;
    }

    public String getCycleEndDate() {
        return cycleEndDate;
    }

    public void setCycleEndDate(String cycleEndDate) {
        this.cycleEndDate = cycleEndDate;
    }

    public String getCycleStartDate() {
        return cycleStartDate;
    }

    public void setCycleStartDate(String cycleStartDate) {
        this.cycleStartDate = cycleStartDate;
    }

    public String getSponsorRefNumber() {
        return sponsorRefNumber;
    }

    public void setSponsorRefNumber(String sponsorRefNumber) {
        this.sponsorRefNumber = sponsorRefNumber;
    }

    public String getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(String unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getSponsoredUnits() {
        return sponsoredUnits;
    }

    public void setSponsoredUnits(String sponsoredUnits) {
        this.sponsoredUnits = sponsoredUnits;
    }

    public String getSponsoredFarmType() {
        return sponsoredFarmType;
    }

    public void setSponsoredFarmType(String sponsoredFarmType) {
        this.sponsoredFarmType = sponsoredFarmType;
    }

    public String getSponsoredFarmRoi() {
        return sponsoredFarmRoi;
    }

    public void setSponsoredFarmRoi(String sponsoredFarmRoi) {
        this.sponsoredFarmRoi = sponsoredFarmRoi;
    }

    public String getSponsorshipDuration() {
        return sponsorshipDuration;
    }

    public void setSponsorshipDuration(String sponsorshipDuration) {
        this.sponsorshipDuration = sponsorshipDuration;
    }

    public long getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(long startPoint) {
        this.startPoint = startPoint;
    }

    public long getTotalAmountPaid() {
        return totalAmountPaid;
    }

    public void setTotalAmountPaid(long totalAmountPaid) {
        this.totalAmountPaid = totalAmountPaid;
    }

    public String getFarmId() {
        return farmId;
    }

    public void setFarmId(String farmId) {
        this.farmId = farmId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
