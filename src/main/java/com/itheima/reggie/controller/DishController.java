package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜品管理
 */
@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增菜品
     *
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        dishService.saveWithFlavor(dishDto);

        return R.success("新增菜品成功");

    }

    /**
     * 分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {

        Page<Dish> pageInfo = new Page<>(page, pageSize);


        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null, Dish::getName, name);
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        dishService.page(pageInfo, queryWrapper);


        Page<DishDto> pageDishDto = new Page<>();
        BeanUtils.copyProperties(pageInfo, pageDishDto, "records");

        List<Dish> records = pageInfo.getRecords();

        List<DishDto> dishDtoPage = records.stream().map(item -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();
            Category byId = categoryService.getById(categoryId);

            String byIdName = byId.getName();
            dishDto.setCategoryName(byIdName);
            return dishDto;
        }).collect(Collectors.toList());

        pageDishDto.setRecords(dishDtoPage);

        return R.success(pageDishDto);

    }

    /**
     * 修改菜品
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable long id) {

        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return R.success(dishDto);
    }

    /**
     * 修改保存菜品
     *
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {

        dishService.updateWithFlavor(dishDto);

        return R.success("新增菜品成功");

    }

    /**
     * 根据条件查询菜品
     *
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());

        queryWrapper.eq(Dish::getStatus, 1);
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        List<DishDto> dishDtos = list.stream().map(item -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String name = category.getName();
                dishDto.setCategoryName(name);
            }
            Long dishId = item.getId();
            List<DishFlavor> flavors = dishFlavorService.list(new LambdaQueryWrapper<DishFlavor>()
                    .eq(DishFlavor::getDishId, dishId));
            dishDto.setFlavors(flavors);
            return dishDto;

        }).collect(Collectors.toList());

        return R.success(dishDtos);
    }
    //@GetMapping("/list")
    //public R<List<Dish>> list(Dish dish){
    //    LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
    //    queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
    //
    //    queryWrapper.eq(Dish::getStatus,1);
    //    queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
    //
    //    List<Dish> list = dishService.list(queryWrapper);
    //
    //    return R.success(list);
    //}

    /**
     * 停售
     *
     * @return
     */
    @PostMapping("/status/0")
    public R statusDish0(@RequestParam Long[] ids) {

        log.info(ids.toString());

        for (Long id : ids) {
            UpdateWrapper updateWrapper = new UpdateWrapper();
            updateWrapper.eq("id", id);
            updateWrapper.set("status", 0);
            boolean update = dishService.update(null, updateWrapper);
            if (!update) {
                return R.error("停售失败");
            }
        }
        return R.success("停售成功");
    }

    /**
     * 起售
     *
     * @return
     */
    @PostMapping("/status/1")
    public R statusDish1(@RequestParam Long[] ids) {

        log.info(ids.toString());

        for (Long id : ids) {
            UpdateWrapper updateWrapper = new UpdateWrapper();
            updateWrapper.eq("id", id);
            updateWrapper.set("status", 1);
            boolean update = dishService.update(null, updateWrapper);
            if (!update) {
                return R.error("启售失败");
            }
        }
        return R.success("启售成功");
    }

    /**
     * 删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public R delete(@RequestParam Long[] ids){
        log.info(ids.toString());

        for (Long id : ids) {

            if (dishService.getById(id).getStatus()==1){
                return R.error("菜品起售");
            }

            dishService.removeById(id);
            LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
            dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId,id);
            dishFlavorService.remove(dishFlavorLambdaQueryWrapper);


        }

        return R.success("删除成功");
    }




}
