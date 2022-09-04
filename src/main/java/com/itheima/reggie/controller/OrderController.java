package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.PageParam;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.AddressBookService;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.beans.Beans;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private AddressBookService addressBookService;

    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("订单数据：{}",orders);
        orderService.submit(orders);
        return R.success("下单成功");
    }

    /**
     * 历史订单
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page> userPage(int page,int pageSize){
        log.info("页面数据{},{}",page,pageSize);
        Page<Orders> orderDetailPage= new Page<>(page,pageSize);
        orderService.page(orderDetailPage);

        /*
        *
        @GetMapping("/page")
        public R<Page> page(int page,int pageSize,String name){
            Page<Setmeal> setmealPage= new Page<>(page,pageSize);

            LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
            setmealLambdaQueryWrapper.like(name!=null,Setmeal::getName,name);
            setmealLambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);
            setmealService.page(setmealPage, setmealLambdaQueryWrapper);

            Page<SetmealDto> setmealDtoPage= new Page<>();

            BeanUtils.copyProperties(setmealPage,setmealDtoPage,"records");

            List<SetmealDto> setmealDtos = setmealPage.getRecords().stream().map(item -> {
                SetmealDto setmealDto = new SetmealDto();
                BeanUtils.copyProperties(item, setmealDto);

                Long categoryId = item.getCategoryId();
                Category byId = categoryService.getById(categoryId);
                if (byId != null) {
                    String categoryName = byId.getName();
                    setmealDto.setCategoryName(categoryName);
                }
                return setmealDto;
            }).collect(Collectors.toList());

            setmealDtoPage.setRecords(setmealDtos);
            return  R.success(setmealDtoPage);

        }

        *
        * */
        if (orderDetailPage!=null){
            return R.success(orderDetailPage);}
       else {
           return R.error("无订单内容");}
    }

    /**
     * 订单明细

     * @return
     */
    @GetMapping("/page")
    public R<IPage<Orders>> Page(PageParam pageParam, String number){
        IPage<Orders> ordersPage = new Page<>(pageParam.getPage(), pageParam.getPageSize());

        LambdaQueryWrapper<Orders> lqw = new LambdaQueryWrapper<>();

        lqw.like(number != null,Orders::getNumber,number);
        lqw.ge(pageParam.getBeginTime() != null,Orders::getOrderTime,pageParam.getBeginTime());
        lqw.le(pageParam.getEndTime() != null,Orders::getOrderTime,pageParam.getEndTime());

       orderService.page(ordersPage,lqw);

        return R.success(ordersPage);

        //log.info("页面数据{},{}",page,pageSize);
        //
        //Page<OrdersDto> ordersDtoPage=new Page<>();
        //
        //Page<Orders> orderPage= new Page<>(page,pageSize);
        //orderService.page(orderPage);
        //
        //BeanUtils.copyProperties(orderPage, ordersDtoPage);
        //
        //List<List<OrderDetail>> collect = orderPage.getRecords().stream().map(item -> {
        //    Long id = item.getId();
        //    LambdaQueryWrapper<OrderDetail> OrderDetailLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //    OrderDetailLambdaQueryWrapper.eq(OrderDetail::getOrderId, id);
        //    List<OrderDetail> list = orderDetailService.list(OrderDetailLambdaQueryWrapper);
        //    return list;
        //}).collect(Collectors.toList());
        //BeanUtils.copyProperties(collect, ordersDtoPage);
        //
        //
        ////List<Orders> collect = orderPage.getRecords().stream().map(item -> {
        ////    Orders orders = new Orders();
        ////    BeanUtils.copyProperties(item, orders);
        ////    Long id = item.getId();
        ////    LambdaQueryWrapper<OrderDetail> OrderDetailLambdaQueryWrapper = new LambdaQueryWrapper<>();
        ////    OrderDetailLambdaQueryWrapper.eq(OrderDetail::getOrderId, id);
        ////    orderDetailService.getOne(OrderDetailLambdaQueryWrapper)
        ////    orders.setAmount(OrderDetail.getAmount());//设置钱
        ////    return orders;
        //
        //
        ////}).collect(Collectors.toList());
        ////
        ////orderPage.setRecords(collect);
        //
        ///*
        //*
        //@GetMapping("/page")
        //public R<Page> page(int page,int pageSize,String name){
        //    Page<Setmeal> setmealPage= new Page<>(page,pageSize);
        //
        //    LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //    setmealLambdaQueryWrapper.like(name!=null,Setmeal::getName,name);
        //    setmealLambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);
        //    setmealService.page(setmealPage, setmealLambdaQueryWrapper);
        //
        //    Page<SetmealDto> setmealDtoPage= new Page<>();
        //
        //    BeanUtils.copyProperties(setmealPage,setmealDtoPage,"records");
        //
        //    List<SetmealDto> setmealDtos = setmealPage.getRecords().stream().map(item -> {
        //        SetmealDto setmealDto = new SetmealDto();
        //        BeanUtils.copyProperties(item, setmealDto);
        //
        //        Long categoryId = item.getCategoryId();
        //        Category byId = categoryService.getById(categoryId);
        //        if (byId != null) {
        //            String categoryName = byId.getName();
        //            setmealDto.setCategoryName(categoryName);
        //        }
        //        return setmealDto;
        //    }).collect(Collectors.toList());
        //
        //    setmealDtoPage.setRecords(setmealDtos);
        //    return  R.success(setmealDtoPage);
        //
        //}
        //
        //*
        //* */
        //if (ordersDtoPage!=null){ return R.success(ordersDtoPage);}
        //else {return R.error("无订单内容");}
    }

    /**
     * 更新订单状态
     * @param orders
     * @return
     */
    @PutMapping
    public R updateStatus(@RequestBody Orders orders){

        log.info(orders.getId().toString());

        UpdateWrapper updateWrapper = new UpdateWrapper();
        updateWrapper.eq("id", orders.getId());
        updateWrapper.set("status",  3);
        boolean update = orderService.update(null, updateWrapper);

        if (update){
            return R.success("状态修改成功！");
        }
        return R.error("状态修改失败");
    }
}