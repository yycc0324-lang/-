package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //密码比对
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }
/**
 * 新增员工
 * @param employeeDTO
 */
    @Override
    public void save(EmployeeDTO employeeDTO) {
        //前端返回的数据为了方便接受使用了DTO封装
        //现在需要给封装的属性拿出来，就可以使用拷贝
        Employee employee = new Employee();//先new一个属性一样的实体，然后把DTO的属性复制给实体
        BeanUtils.copyProperties(employeeDTO, employee);//(需要复制的内容，自己的空实体)
        //接下来需要设置employeeDTO中没有的但是Employee有的属性（当然不设置也可以，
        // 否则就直接在employeeDTO中写全属性了）


        //设置账号状态
        employee.setStatus(StatusConstant.ENABLE);

        //设置密码默认
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        //设置创建时间和更新时间为系统时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        //TODO 设置创建人和修改人ID，即登陆用户，先写死
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());


        /*
        * 插入（新增）员工数据
        * */
        employeeMapper.insert(employee);
    }

    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        //分页查询使用PageHelper去封装，此处用到插件是mybatis-pagehelper
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        //获取分页结果，写好方法到EmployeeMapper中
        Page<Employee>  page = employeeMapper.pageQuery(employeePageQueryDTO);
        //对page进行处理成pageresult
        long total = page.getTotal();//总数
        List records = page.getResult();//获取数据，total和records都是文档要求且企业中固定需要分页查询这样写的写法
        return new PageResult(total, records);//可以在返回页直接new一个PageResult
    }

    @Override
    public void startOrStop(Integer status, Long id) {
              /*  Employee employee = Employee.builder()
                        .status(status)
                        .id(id)
                        .updateTime(LocalDateTime.now())
                        .updateUser(BaseContext.getCurrentId())
                        .build();*/
        Employee employee = new Employee();
        employee.setStatus(status);
        employee.setId(id);
        employeeMapper.startOrStop(employee);

    }

    @Override
    public Employee getById(Long id) {
        return employeeMapper.getById(id);
    }


}
