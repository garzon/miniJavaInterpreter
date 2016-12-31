import java.lang.*;
import java.util.HashMap;

import miniJava.*;

/**
 * Created by ougar_000 on 2016/12/30.
 */

public class MiniJavaEvaluator extends MiniJavaTypeChecker {

    @Override public MiniJavaVar visitStmtBlock(MiniJavaParser.StmtBlockContext ctx) {
        varCtx.enterBlock();
        for(MiniJavaParser.StmtContext stmt: ctx.stmt()) {
            if(visit(stmt).isError()) return MiniJavaVar.makeError();
        }
        varCtx.exitBlock();
        return MiniJavaVar.makeVoid();
    }

    @Override public MiniJavaVar visitSystemCall(MiniJavaParser.SystemCallContext ctx) {
        MiniJavaVar v = visit(ctx.exp());
        return Eval.visitSystemCall(ctx, v);
    }

    @Override public MiniJavaVar visitVarDeclaration(MiniJavaParser.VarDeclarationContext ctx) {
        return Eval.visitVarDeclaration(ctx, varCtx);
    }

    @Override public MiniJavaVar visitId(MiniJavaParser.IdContext ctx) {
        String id = ctx.ID().getText();
        return idFoundOrNot(ctx, varCtx, id);
    }

    @Override public MiniJavaVar visitAssign(MiniJavaParser.AssignContext ctx) {
        String assignSym = ctx.assignSym().getText();
        String id = ctx.ID().getText();

        MiniJavaVar findRes = idFoundOrNot(ctx, varCtx, id);
        if(findRes.isError()) return findRes;

        if(!assignSym.equals("=")) {
            if(findRes.value == null) {
                System.err.printf("[ERR] Variable '%s' used in '%s' before being initialized.\n", id, ctx.getText());
                return MiniJavaVar.makeError();
            }
        }

        MiniJavaVar v = visit(ctx.exp());
        if(v.isError()) return v;

        if(!checkAssignOprType(ctx, v.type, findRes.type)) return MiniJavaVar.makeError();
        if(assignSym.equals("=")) return varCtx.assignVar(id, v);

        if(!checkAssignOprType(ctx, findRes.type, "int")) return MiniJavaVar.makeError();
        if(assignSym.equals("*=")) { findRes.value = (int)findRes.value * (int)v.value; return findRes; }
        if(assignSym.equals("/=")) { findRes.value = (int)findRes.value / (int)v.value; return findRes; }
        if(assignSym.equals("%=")) { findRes.value = (int)findRes.value % (int)v.value; return findRes; }
        if(assignSym.equals("&=")) { findRes.value = (int)findRes.value & (int)v.value; return findRes; }
        if(assignSym.equals("|=")) { findRes.value = (int)findRes.value | (int)v.value; return findRes; }
        if(assignSym.equals("^=")) { findRes.value = (int)findRes.value ^ (int)v.value; return findRes; }
        if(assignSym.equals("+=")) { findRes.value = (int)findRes.value + (int)v.value; return findRes; }
        if(assignSym.equals("-=")) { findRes.value = (int)findRes.value - (int)v.value; return findRes; }
        if(assignSym.equals("<<=")) { findRes.value = (int)findRes.value << (int)v.value; return findRes; }
        if(assignSym.equals(">>>=")) { findRes.value = (int)findRes.value >>> (int)v.value; return findRes; }
        if(assignSym.equals(">>=")) { findRes.value = (int)findRes.value >> (int)v.value; return findRes; }

        System.err.printf("[ERR] Assignment of '%s' is not implemented yet.\n", assignSym);
        return MiniJavaVar.makeError();
    }

    @Override public MiniJavaVar visitUnaryOp(MiniJavaParser.UnaryOpContext ctx) {
        MiniJavaVar res = visit(ctx.first);
        return unaryOp(ctx, res);
    }

    @Override public MiniJavaVar visitBinaryOp(MiniJavaParser.BinaryOpContext ctx) {
        MiniJavaVar first = visit(ctx.first);
        MiniJavaVar second = visit(ctx.second);
        return binaryOp(ctx, first, second);
    }

    @Override public MiniJavaVar visitIntLiteral(MiniJavaParser.IntLiteralContext ctx) {
        if(ctx.INT_BIN() != null) {
            return MiniJavaVar.makeInt(Integer.parseInt(ctx.getText().substring(2), 2));
        }
        if(ctx.INT_DEC() != null) {
            return MiniJavaVar.makeInt(Integer.parseInt(ctx.getText()));
        }
        if(ctx.INT_HEX() != null) {
            return MiniJavaVar.makeInt(Integer.parseInt(ctx.getText().substring(2), 16));
        }
        System.err.println("Unknown type of int literal.");
        return MiniJavaVar.makeError();
    }

}