package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;


    /**
     * 保存套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        log.info(setmealDto.toString());
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }

    /**
     * 套餐分页查询
     * @return
     */
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

    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        setmealService.removeWithDish(ids);
        return R.success("删除成功");
    }

    /**
     * 根据条件查询套餐
     * @param
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list( Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());

        queryWrapper.eq(Setmeal::getStatus,1);

        queryWrapper.orderByAsc(Setmeal::getStatus).orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(queryWrapper);

        return R.success(list);
    }


    /**
     * 停售
     * @return
     */
    @PostMapping("/status/1")
    public R status1(@RequestParam List<String> ids){

        for (String id : ids) {
            UpdateWrapper updateWrapper = new UpdateWrapper();
            updateWrapper.eq("id", id);
            updateWrapper.set("status", 1);
            boolean update = setmealService.update(null, updateWrapper);
            if (!update) {
                return R.error("启售失败");
            }
        }
        return R.success("起售成功");
    }

    /**
     * 停售
     * @return
     */
    @PostMapping("/status/0")
    public R status0(@RequestParam List<String> ids){

        for (String id : ids) {
            UpdateWrapper updateWrapper = new UpdateWrapper();
            updateWrapper.eq("id", id);
            updateWrapper.set("status", 0);
            boolean update = setmealService.update(null, updateWrapper);
            if (!update) {
                return R.error("启售失败");
            }
        }
        return R.success("起售成功");
    }


    /**
     *  回显
     * @return
     */
    @GetMapping("/{id}")
    public R getById(@PathVariable Long id){

        Setmeal setmeal = setmealService.getById(id);

        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SetmealDish::getSetmealId,id);
        List<SetmealDish> list = setmealDishService.list(lambdaQueryWrapper);

        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal,setmealDto);
        setmealDto.setSetmealDishes(list);

        return R.success(setmealDto);
    }

    /**
     * 保存
     * @return
     */
    @PutMapping
    public R update(@RequestBody SetmealDto setmealDto){
        setmealService.updateById(setmealDto);

        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.eq(SetmealDish::getDishId,setmealDto.getId());
        setmealDishService.remove(setmealDishLambdaQueryWrapper);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map(item -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        boolean b = setmealDishService.saveBatch(setmealDishes);

        if (b){
            return R.success("保存成功");
        }
        return R.error("保存失败");


    }



}
