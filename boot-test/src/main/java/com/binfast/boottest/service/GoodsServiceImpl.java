package com.binfast.boottest.service;

import com.binfast.adpter.core.annotations.ApiMapping;
import com.binfast.adpter.core.annotations.GetApiMapping;
import com.binfast.adpter.core.annotations.PostApiMapping;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;

/**
 * @apiNote 商品管理
 */
@Service
@ApiMapping(notes = "商品管理")
public class GoodsServiceImpl {
	//无缝集成

	/**
	 * @apiNote 增加商品
	 * @param goods
	 * @param id
	 * @return
	 */
	@PostApiMapping(value = "/goods/add", notes = "增加商品")
	public Goods addGoods(Goods goods,
						  Integer id
						  ){
		return goods;
	}

	/**
	 * @apiNote 商品明细
	 * @param id
	 * @return
	 */
	@GetApiMapping(value = "/goods/get", notes = "商品明细")
	public Goods getGodds(Integer id){
		return new Goods("vvv", "1111");
	}
	public static class Goods implements Serializable{
		private String goodsName;
		private String goodsId;
		private List<String> labels;
		public Goods(){

		}
		public Goods(String goodsName, String goodsId) {
			this.goodsName = goodsName;
			this.goodsId = goodsId;
		}


		public List<String> getLabels() {
			return labels;
		}

		public void setLabels(List<String> labels) {
			this.labels = labels;
		}

		public String getGoodsName() {
			return goodsName;
		}
		public void setGoodsName(String goodsName) {
			this.goodsName = goodsName;
		}
		public String getGoodsId() {
			return goodsId;
		}
		public void setGoodsId(String goodsId) {
			this.goodsId = goodsId;
		}
	}
}
