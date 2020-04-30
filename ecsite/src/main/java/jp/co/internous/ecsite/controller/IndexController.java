package jp.co.internous.ecsite.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

import jp.co.internous.ecsite.model.dao.GoodsRepository;
import jp.co.internous.ecsite.model.dao.PurchaseRepository;
import jp.co.internous.ecsite.model.dao.UserRepository;
import jp.co.internous.ecsite.model.dto.HistoryDto;
import jp.co.internous.ecsite.model.dto.LoginDto;
import jp.co.internous.ecsite.model.entity.Goods;
import jp.co.internous.ecsite.model.entity.Purchase;
import jp.co.internous.ecsite.model.entity.User;
import jp.co.internous.ecsite.model.form.CartForm;
import jp.co.internous.ecsite.model.form.HistoryForm;
import jp.co.internous.ecsite.model.form.LoginForm;

@Controller
@RequestMapping("/ecsite")
public class IndexController { 
	
	//@Autowired=インスタンス化
	@Autowired//UserエンティティからuserテーブルにアクセスするDAO
	private UserRepository userRepos;
	
	@Autowired//GoodsエンティティからgoodsテーブルにアクセスするDAO
	private GoodsRepository goodsRepos;
	
	@Autowired//PurchaseエンティティからpurchaseテーブルにアクセスするDAO
	private PurchaseRepository purchaseRepos;
	private Gson gson = new Gson();//Gson＝JSON形式に変換（google提供）
	
	@RequestMapping("/")//トップページ（index.html）に遷移
	//goodsテーブルから取得した商品エンティティの一覧を、フロントに渡すModelに追加
	public String index(Model m) {
		List<Goods> goods = goodsRepos.findAll();
		//商品名,index.htmlの<tr th:each="item: ${goods}">を下記の引数goodsに代入
		m.addAttribute("goods", goods);
		//index=index.html
		return "index";
		
	}
	
	@ResponseBody
	@PostMapping("/api/login")
	public String loginApi(@RequestBody LoginForm form) {
		//userReposのuserName,Passwordを取得し、userをList化
		List<User> users = userRepos.findByUserNameAndPassword(form.getUserName(), form.getPassword());
		LoginDto dto = new LoginDto(0, null, null, "ゲスト");
		//ゲストと既存の登録者の設定
		if (users.size() > 0 ) {
			dto = new LoginDto(users.get(0));
		}
		//上記のdtoをJSON化
		return gson.toJson(dto);
	}
	
	@ResponseBody
	@PostMapping("/api/purchase")
	//CartForm=fとして設定,購入ボタン押下時
	public String purchaseApi(@RequestBody CartForm f) {
		//List型で<Cart>を返すとforEachの引数c=Cartのインスタンスが一つずつ順番に入っていく
		f.getCartList().forEach((c) -> {
			//合計金額＝値段×数
			long total = c.getPrice() * c.getCount();
			//purchaseRepos.persistから取得、DBに記録
			purchaseRepos.persist(f.getUserId(), c.getId(), c.getGoodsName(), c.getCount(), total);
		});
		return String.valueOf(f.getCartList().size());
	}
	
	@ResponseBody
	@PostMapping("/api/history")
	//履歴ボタン押下時
	public String historyApi(@RequestBody HistoryForm form) {
	//UserId取得、購入履歴取得(v=parchase)、購入履歴リスト作成
		String userId = form.getUserId();
		List<Purchase> history = purchaseRepos.findHistory(Long.parseLong(userId));
		List<HistoryDto> historyDtoList = new ArrayList<>();
		history.forEach((v) -> {
			HistoryDto dto = new HistoryDto(v);
			historyDtoList.add(dto);
		});
		//購入履歴リストのJSON化
		return gson. toJson(historyDtoList);
	}
}

