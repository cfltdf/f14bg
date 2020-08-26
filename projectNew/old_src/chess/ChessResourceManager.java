package com.f14.chess;

import com.f14.F14bg.consts.GameType;
import com.f14.F14bg.manager.ResourceManager;
import com.f14.bg.action.BgResponse;
import com.f14.bg.utils.BgUtils;
import com.f14.utils.ExcelUtils;
import com.f14.utils.SequenceUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.util.ArrayList;
import java.util.Collection;

public class ChessResourceManager extends ResourceManager {


    protected Collection<ChessPiece> pieces = new ArrayList<>();

    @Override
    protected BgResponse createResourceResponse() {
        BgResponse res = super.createResourceResponse();
        res.setPublicParameter("pieces", this.pieces);
        return res;
    }


    @Override
    public GameType getGameType() {
        return GameType.Chess;
    }

    @Override
    public void init() throws Exception {
        HSSFWorkbook wb = new HSSFWorkbook(BgUtils.getFileInputStream("./boardGame/Chess.xls"));
        HSSFSheet sheet = wb.getSheetAt(0);
        String[] head = ExcelUtils.rowToStringArray(sheet.getRow(0));
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            ChessPiece piece = ExcelUtils.rowToObject(sheet.getRow(i), head, ChessPiece.class);
            for (ChessPosition pos : piece.startPositions) {
                ChessPiece p = (ChessPiece) piece.clone();
                p.id = SequenceUtils.generateId(ChessPiece.class);
                p.startPosition = pos;
                pieces.add(p);
            }
        }
        super.init();
    }


    public Collection<ChessPiece> getPieces() {
        return pieces;
    }

}
