package com.f14.machikoro;

import com.f14.F14bg.consts.GameType;
import com.f14.F14bg.manager.ResourceManager;
import com.f14.bg.action.BgResponse;
import com.f14.bg.utils.BgUtils;
import com.f14.machikoro.card.MachiCard;
import com.f14.utils.ExcelUtils;
import com.f14.utils.SequenceUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.util.ArrayList;
import java.util.List;

public class MachiResourceManager extends ResourceManager {

    protected List<List<MachiCard>> startCards = new ArrayList<>(4);

    protected List<MachiCard> cards = new ArrayList<>();

    protected void addCard(MachiCard card) {
        if (card.isStart()) {
            for (int i = 0; i < card.qty; ++i) {
                MachiCard c = (MachiCard) card.clone();
                c.id = SequenceUtils.generateId(MachiKoro.class);
                if (startCards.get(i) == null) {
                    startCards.set(i, new ArrayList<>());
                }
                startCards.get(i).add(c);
            }
        } else {
            for (int i = 0; i < card.qty; ++i) {
                MachiCard c = (MachiCard) card.clone();
                c.id = SequenceUtils.generateId(MachiKoro.class);
                cards.add(c);
            }
        }
    }

    @Override
    public BgResponse createResourceResponse() {
        BgResponse res = super.createResourceResponse();
        List<MachiCard> allCards = new ArrayList<>();
        allCards.addAll(this.cards);
        for (List<MachiCard> mc : this.startCards) {
            allCards.addAll(mc);
        }
        res.setPublicParameter("cards", allCards);
        return res;
    }


    public List<MachiCard> getCards() {
        return cards;
    }


    @Override
    public GameType getGameType() {
        return GameType.Machikoro;
    }


    public List<List<MachiCard>> getStartCards() {
        return startCards;
    }

    @Override
    public void init() throws Exception {
        HSSFWorkbook wb = new HSSFWorkbook(BgUtils.getFileInputStream("./boardGame/Machikoro.xls"));
        // 第一个sheet是卡牌的信息
        HSSFSheet sheet = wb.getSheetAt(0);
        String[] head = ExcelUtils.rowToStringArray(sheet.getRow(0));
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            try {
                MachiCard card = ExcelUtils.rowToObject(sheet.getRow(i), head, MachiCard.class);
                card.id = SequenceUtils.generateId(MachiKoro.class);
                this.addCard(card);
            } catch (Exception e) {
                log.error("转换卡牌信息时发生错误! i=" + i, e);
                throw e;
            }
        }
    }

}
