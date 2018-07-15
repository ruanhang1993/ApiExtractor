package cn.edu.fudan.se.apiChangeExtractor.ast;

import japa.parser.ast.Node;
import japa.parser.ast.body.AnnotationDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.comments.Comment;
import japa.parser.ast.comments.LineComment;
import japa.parser.ast.stmt.AssertStmt;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.BreakStmt;
import japa.parser.ast.stmt.ContinueStmt;
import japa.parser.ast.stmt.DoStmt;
import japa.parser.ast.stmt.EmptyStmt;
import japa.parser.ast.stmt.ExplicitConstructorInvocationStmt;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.ForStmt;
import japa.parser.ast.stmt.ForeachStmt;
import japa.parser.ast.stmt.IfStmt;
import japa.parser.ast.stmt.LabeledStmt;
import japa.parser.ast.stmt.ReturnStmt;
import japa.parser.ast.stmt.SwitchStmt;
import japa.parser.ast.stmt.SynchronizedStmt;
import japa.parser.ast.stmt.ThrowStmt;
import japa.parser.ast.stmt.TryStmt;
import japa.parser.ast.stmt.TypeDeclarationStmt;
import japa.parser.ast.stmt.WhileStmt;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import javax.sound.sampled.Line;

public abstract class TreeConverter extends VoidVisitorAdapter<Object>{
//	private CodeTree result;

    protected abstract CodeTree convert(MethodDeclaration n);

    protected abstract CodeTree convert(AssertStmt n);

    protected abstract CodeTree convert(BlockStmt n);

    protected abstract CodeTree convert(BreakStmt n);

    protected abstract CodeTree convert(ContinueStmt n);

    protected abstract CodeTree convert(DoStmt n);

    protected abstract CodeTree convert(EmptyStmt n);

    protected abstract CodeTree convert(ExpressionStmt n);

    protected abstract CodeTree convert(ForeachStmt n);

    protected abstract CodeTree convert(ForStmt n);

    protected abstract CodeTree convert(IfStmt n);

    protected abstract CodeTree convert(LabeledStmt n);

    protected abstract CodeTree convert(ReturnStmt n);

    protected abstract CodeTree convert(SynchronizedStmt n);

    protected abstract CodeTree convert(TryStmt n);

    protected abstract CodeTree convert(TypeDeclarationStmt n);

    protected abstract CodeTree convert(WhileStmt n);

    protected abstract CodeTree convert(ExplicitConstructorInvocationStmt n);

    protected abstract CodeTree convert(SwitchStmt n);

    protected abstract CodeTree convert(ThrowStmt n);

    protected abstract CodeTree convert(LineComment n);

    protected abstract CodeTree newInstance(Node n);

    @Override
    public void visit(MethodDeclaration n, Object arg) {
        convert(n);
    }

    @Override
    public void visit(AssertStmt n, Object arg) {
        convert(n);
    }

    @Override
    public void visit(BlockStmt n, Object arg) {
        convert(n);
    }

    @Override
    public void visit(BreakStmt n, Object arg) {
        convert(n);
    }

    @Override
    public void visit(ContinueStmt n, Object arg) {
        convert(n);
    }

    @Override
    public void visit(DoStmt n, Object arg) {
        convert(n);
    }

    @Override
    public void visit(EmptyStmt n, Object arg) {
        convert(n);
    }

    @Override
    public void visit(ExpressionStmt n, Object arg) {
        convert(n);
    }

    @Override
    public void visit(ForeachStmt n, Object arg) {
        convert(n);
    }

    @Override
    public void visit(ForStmt n, Object arg) {
        convert(n);
    }

    @Override
    public void visit(IfStmt n, Object arg) {
        convert(n);
    }

    @Override
    public void visit(LabeledStmt n, Object arg) {
        convert(n);
    }

    @Override
    public void visit(ReturnStmt n, Object arg) {
        convert(n);
    }

    @Override
    public void visit(SynchronizedStmt n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(TryStmt n, Object arg) {
        convert(n);
    }

    @Override
    public void visit(TypeDeclarationStmt n, Object arg) {
        convert(n);
    }

    @Override
    public void visit(WhileStmt n, Object arg) {
        convert(n);
    }

    @Override
    public void visit(ExplicitConstructorInvocationStmt n, Object arg) {
        convert(n);
    }

    @Override
    public void visit(SwitchStmt n, Object arg) {
        convert(n);
    }

    @Override
    public void visit(ThrowStmt n, Object arg) {
        convert(n);
    }

    @Override
    public void visit(LineComment n, Object arg){convert(n);}
//	public CodeTree getCodeTree() {
//		return result;
//	}

}
