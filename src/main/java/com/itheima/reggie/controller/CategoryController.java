package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增菜品
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Category category){
        categoryService.save(category);
        return R.success("新增分类成功");
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize ){
        Page<Category> pageInfo =new Page<>(page,pageSize);

        LambdaQueryWrapper<Category> queryWrapper=new LambdaQueryWrapper<>();

        queryWrapper.orderByAsc(Category::getSort);

        categoryService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);

    }

    /**
     * 删除分类
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long ids){
        log.info("删除分类id{}",ids);

        //categoryService.removeById(id);
        categoryService.remove(ids);
        return R.success("删除成功");
    }

    /**
     * 修改
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Category category){
       log.info("修改信息{}",category);
       categoryService.updateById(category);
     return   R.success("修改分类信息成功");
    }

    /**
     * 返回下拉分类数据
     * @param category
     * @return
     */
  @GetMapping("/list")
    public R<List<Category>> list(Category category){
        LambdaQueryWrapper<Category> QueryWrapper = new LambdaQueryWrapper<>();
        QueryWrapper.eq(category.getType()!=null,Category::getType,category.getType());
        QueryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
      List<Category> list = categoryService.list(QueryWrapper);


      return R.success(list);

  }

}
