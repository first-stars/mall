package com.w.gulimall.ware.service.impl;

import com.w.common.constant.WareConstant;
import com.w.gulimall.ware.entity.PurchaseDetailEntity;
import com.w.gulimall.ware.service.PurchaseDetailService;
import com.w.gulimall.ware.service.WareSkuService;
import com.w.gulimall.ware.vo.MergeVo;
import com.w.gulimall.ware.vo.PurchaseDoneVo;
import com.w.gulimall.ware.vo.PurchaseItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.w.common.utils.PageUtils;
import com.w.common.utils.Query;

import com.w.gulimall.ware.dao.PurchaseDao;
import com.w.gulimall.ware.entity.PurchaseEntity;
import com.w.gulimall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    private PurchaseDetailService purchaseDetailService;

    @Autowired
    private WareSkuService WareSkuService;



    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceive(Map<String, Object> params) {


        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status",0).or().eq("status",1)
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        if (purchaseId==null){
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            this.save(purchaseEntity);

            purchaseId=purchaseEntity.getId();
        }

        //TODO 确认采购单状态是0，1才可以合并
        PurchaseEntity purchase = this.getById(purchaseId);
        if (purchase.getStatus() == WareConstant.PurchaseStatusEnum.RECEIVE.getCode()||
                purchase.getStatus() == WareConstant.PurchaseStatusEnum.FINISH.getCode()||
                purchase.getStatus() == WareConstant.PurchaseStatusEnum.HASERROR.getCode()){
            try {
                throw new Exception();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }


        List<Long> items = mergeVo.getItems();

        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> collect = items.stream().map(i -> {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();

            purchaseDetailEntity.setId(i);
            purchaseDetailEntity.setPurchaseId(finalPurchaseId);
            purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
            return purchaseDetailEntity;

        }).collect(Collectors.toList());

        purchaseDetailService.updateBatchById(collect);

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);

    }

    @Override
    public void received(List<Long> ids) {
        //1、确认当前采购单是新建或分配状态
        List<PurchaseEntity> collect = ids.stream().map(id -> {
            PurchaseEntity purchaseEntity = this.getById(id);
            return purchaseEntity;
        }).filter(item -> {
            if (item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode() ||
                    item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()) {
                return true;
            } else {
                return false;
            }
        }).map(item -> {
            item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
            item.setUpdateTime(new Date());
            return item;
        }).collect(Collectors.toList());

        //2、改变采购单的状态
        this.updateBatchById(collect);

        //3、改变采购项的状态
        collect.forEach(item -> {
            List<PurchaseDetailEntity> entities = purchaseDetailService.listDetailByPurchaseId(item.getId());
            List<PurchaseDetailEntity> collect1 = entities.stream().map(entity -> {
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                purchaseDetailEntity.setId(entity.getId());
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.RECEIVE.getCode());
                return purchaseDetailEntity;
            }).collect(Collectors.toList());

            purchaseDetailService.updateBatchById(collect1);
        });
    }

    @Transactional
    @Override
    public void done(PurchaseDoneVo doneVo) {

        //2、改变采购性状态
        List<PurchaseItemVo> items = doneVo.getItems();
        List<PurchaseDetailEntity> purchaseDetailEntities = new ArrayList<>();
        boolean flag=true;
        for (PurchaseItemVo item : items) {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            if (item.getItemId()==WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode()){
                purchaseDetailEntity.setStatus(item.getStatus());
                flag=false;
            }else {
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISH.getCode());
                //3、将成功采购的进行入库
                PurchaseDetailEntity byId = purchaseDetailService.getById(item.getItemId());
                WareSkuService.addStock(byId.getSkuId(),byId.getSkuNum(),byId.getWareId());
            }
            purchaseDetailEntity.setId(item.getItemId());
            purchaseDetailEntities.add(purchaseDetailEntity);
        }
        purchaseDetailService.updateBatchById(purchaseDetailEntities);

        //1、改变采购单状态
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(doneVo.getId());
        purchaseEntity.setStatus(flag?WareConstant.PurchaseStatusEnum.FINISH.getCode():WareConstant.PurchaseStatusEnum.HASERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);




    }

}