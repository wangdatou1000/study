import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.datou.springstudy.dal.dao.UserMapper;
import com.datou.springstudy.dal.model.User;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:configs/spring-mybatis.xml")
public class UserDaoTest extends AbstractJUnit4SpringContextTests {
	@Autowired
	private UserMapper userMapper;

	@Test
	public void UserMapperTest() {
		int userId = 1;
		User user = userMapper.selectByPrimaryKey(Integer.valueOf(userId));
		if (user != null)
			assertEquals(Integer.valueOf(userId), user.getIduser());
		int time2 = (int) (System.currentTimeMillis() / 1000);
		user = new User();
		user.setUserName("wangdatou");
		user.setTel("1111111");
		user.setCreateTime(Integer.valueOf(time2));
		user.setUpdateTime(Integer.valueOf(time2));
		int cus;
		// userMapper.insertSelective(user);

		time2 = (int) (System.currentTimeMillis() / 1000);
		user = new User();
		user.setIduser(Integer.valueOf(1));
		user.setUpdateTime(time2);
		cus = userMapper.updateByPrimaryKeySelective(user);
		String ints = Integer.MAX_VALUE + "";
		System.out.println(ints + "---" + ints.length());
		// User user = test.selectByPrimaryKey(Integer.valueOf(1));
		System.out.println(user.getUserName() + time2 + "--" + cus);
	}
}
