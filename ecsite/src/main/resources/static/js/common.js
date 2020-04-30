let login = (event) => {
	//index.html<form name="loginForm" action="#">というデフォルトを妨害・キャンセルし、下記(一部を動的に変更)処理を実行 
	event.preventDefault();
	//jsonStringにuserName,passwordをログイン者の入力したものとして代入し、$.ajaxのdata:JSON.StringifyでJSON文字列に変換
	let jsonString = {
			'userName': $('input[name=userName]').val(),
			'password': $('input[name=password]').val()
	};
	//非同期通信(画面遷移しない）,画面内の一部を動的に変更
	$.ajax({
		//POST＝データの送信
		type: 'POST',
		//データの送信先
		url: '/ecsite/api/login',
		//javaScriptのオブジェクトデータをjson化
		data: JSON.stringify(jsonString),
		contentType: 'application/json',
		datatype: 'json',
		//文字化け防止
		scriptCharset: 'utf-8'
	})
	//結果、非同期通信で返す
	.then((result) => {
		//JSONをjavaScriptで再び使いやすい形（オブジェクトデータ）に変換
		let user = JSON.parse(result);
		//$｛テーブル名.カラム名｝でDBの情報取得
		//index.htmlの<span id="welcome"> -- ようこそ！　ゲスト　さん</span>を書き換え
		$('#welcome').text(`　-- ようこそ！ ${user.fullName} さん`);
		//index.htmlの<input id="hiddenUserId" value="0">をvalue="user.id"に上書き
		$('#hiddenUserId').val(user.id);
		//ログイン後、入力欄を空にするためval('')で表記,後述のtbody子要素のremoveでの初期化処理と関連
		$('input[name=userName]').val('');
		$('input[name=password]').val('');
	}, () => {
		//エラー時コンソールに表示
		console.error('Error: ajax connection failed. ');
		}
	);
};

let addCart = (event) => {
	let tdList = $(event.target).parent().parent().find('td');
	
	let id = $(tdList[0]).text();
	let goodsName = $(tdList[1]).text();
	let price = $(tdList[2]).text();
	let count = $(tdList[3]).find('input').val();
	
	if (count === '0' || count === ''){
		alert('注文数が０または空欄です。')
		return;
	}
	
	let cart = {
			'id' : id,
			'goodsName': goodsName,
			'price': price,
			'count': count
	};
	//index.htmlの<script>内のlet cartListにpush（=要素を追加）
	cartList.push(cart);
	
	let tbody = $('#cart').find('tbody');
	//（元々tbody内に入っていたものが次回ログイン時に再追加され重複カウントされないように削除し）初期化
	$(tbody).children().remove();
//表示部分
	//forEach＝複数の配列のループ処理（ボタンを押すとcartの要素とindex番号を両方繰り返し処理）
	cartList.forEach(function(cart, index){
		let tr = $('<tr />');
		//cartに移動するとき、cartの中に入ってるidをtext型で出力、<tr>に<td>（text型でid等）を追加
		$('<td />', { 'text': cart.id }).appendTo(tr);
		$('<td />', { 'text': cart.goodsName }).appendTo(tr);
		$('<td />', { 'text': cart.price }).appendTo(tr);
		$('<td />', { 'text': cart.count }).appendTo(tr);
		let tdButton = $('<td />');
		$('<button />', {
			'text': 'カート削除',
			'class': 'removeBtn',
		}).appendTo(tdButton);
		
		$(tdButton).appendTo(tr);
		$(tr).appendTo(tbody);
	});
	//削除ボタン自体は購入したら表示されないといけないので先に作成し、押下時の設定は後述(let removeCart)
	$('.removeBtn').on('click', removeCart);
	
};

let buy = (event) => {
	$.ajax({
		type: 'POST',
		url: '/ecsite/api/purchase',
		data: JSON.stringify({
			"userId": $('#hiddenUserId').val(),
			"cartList": cartList
		}),
		contentType: 'application/json',
		datatype: 'json',
		scriptCharset: 'utf-8'
	})
	.then((result) => {
		alert('購入しました。');
		}, () => {
		console.error('Error: ajax connection failed. ');
		}
	);
};

let removeCart = (event) => {
	//const書き換え不可能変数
	const tdList = $(event.target).parent().parent().find('td');
	//tdListの配列0番目のテキストを代入
	let id = $(tdList[0]).text();
	//filter=抽出
	cartList = cartList.filter(function(cart) {
		//下記条件のとき抽出
		return cart.id !== id;
	});
	//おじいさん要素を探してtdListに代入されているので、見つけたものを消す
	$(event.target).parent().parent().remove();
};

let showHistory = () => {
	//DBに介入
	$.ajax({
		//POSTでURLに送信
		type: 'POST',
		url: '/ecsite/api/history',
		//javaScriptのオブジェクトデータをjson化、value=値（userIdを入手）
		data: JSON.stringify({ "userId": $('#hiddenUserId').val() }),
		contentType: 'application/json',
		datatype: 'json',
		//文字化け防止
		scriptCharset: 'utf-8'
	})	
	.then((result) => {
		//	JSONをjavaScriptで再び使いやすいオブジェクトデータに変換
		let historyList = JSON.parse(result);
		let tbody = $('#historyTable').find('tbody');
		//最新の履歴のみ表示（以前の履歴を削除）
		$(tbody).children().remove();
		//foreachは複数の引数（historyの要素とindex要素番号)を繰り返し処理＝履歴の取得、順番で表示（）
		historyList.forEach((history,index) => {
			//index.htmlの<tr>を引きだす
			let tr = $('<tr />');
			//trに｛'text'~~｝を追加　htmlのthの下に追加＝履歴の表が完成
			$('<td />', { 'text': history.goodsName }).appendTo(tr);
			$('<td />', { 'text': history.itemCount }).appendTo(tr);
			$('<td />', { 'text': history.createdAt }).appendTo(tr);
			//historyTableはtbody(index.html)に追加
			$(tr).appendTo(tbody);
		});
		//dialog表示(index.html l.86のid)
		$("#history").dialog("open");
	}, () => {
		//エラー時コンソールに表示
		console.error('Error: ajax connection failed. ');
		}
	);
}