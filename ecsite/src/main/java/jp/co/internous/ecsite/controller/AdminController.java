package jp.co.internous.ecsite.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jp.co.internous.ecsite.model.dao.GoodsRepository;
import jp.co.internous.ecsite.model.dao.UserRepository;
import jp.co.internous.ecsite.model.entity.Goods;
import jp.co.internous.ecsite.model.entity.User;
import jp.co.internous.ecsite.model.form.GoodsForm;
import jp.co.internous.ecsite.model.form.LoginForm;

@Controller
@RequestMapping("/ecsite/admin")
public class AdminController {

	//@Autowired=インスタンス化
	@Autowired
	private UserRepository userRepos;

	@Autowired
	private GoodsRepository goodsRepos;
	
	//トップページ（adminindex.html）に遷移
	@RequestMapping("/")
	public String index(){
		return "adminindex";
	}

	@PostMapping("/welcome")
	//ユーザ名とパスワードをLoginFormを介して受け取り、ユーザー検索
	public String welcome(LoginForm form, Model m){
		List<User> users = userRepos.findByUserNameAndPassword(form.getUserName(), form.getPassword());

		//検索結果が存在していればisAdmin（管理者かどうか）を取得し、管理者だった場合のみ処理 
		if(users != null && users.size() > 0){
			boolean isAdmin = users.get(0).getIsAdmin() != 0;
			if(isAdmin){
				List<Goods> goods = goodsRepos.findAll();
				m.addAttribute("userName", users.get(0).getUserName());
				m.addAttribute("password", users.get(0).getPassword());
				m.addAttribute("goods", goods);
			}
		}
		return "welcome";
	}
	
	@RequestMapping("/goodsMst")
	public String goodsMst(LoginForm form, Model m){
		m.addAttribute("userName", form.getUserName());
		m.addAttribute("password", form.getPassword());

		return "goodsmst";
	}
	
	//新規商品登録機能
	@RequestMapping("/addGoods")
	public String addGoods(GoodsForm goodsForm, LoginForm loginForm, Model m){
		m.addAttribute("userName", loginForm.getUserName());
		m.addAttribute("password", loginForm.getPassword());

		Goods goods = new Goods();
		goods.setGoodsName(goodsForm.getGoodsName());
		goods.setPrice(goodsForm.getPrice());
		goodsRepos.saveAndFlush(goods);

		return "forward:/ecsite/admin/welcome";
	}
	
	//商品削除機能（ページ遷移ではなくREST=ajaxを使用する処理）
	@ResponseBody
	@PostMapping("/api/deleteGoods")
	public String deleteApi(@RequestBody GoodsForm f, Model m) {
		try{
			goodsRepos.deleteById(f.getId());
		} catch (IllegalArgumentException e) {
			return "-1";
		}

		return "1";
		/*商品削除時に/api/deleteGoodsが呼ばれ、結果をJSが見に行くが、web上では文字列でしか書き出せない。
		通常関数を呼んでreturnで戻ってくるのであればbooleanであるが、今回はtrue,falseという文字と一致しているかではないので、1,-1で代用し、真偽値ではないと明確に表記。*/
	} 
	
}
