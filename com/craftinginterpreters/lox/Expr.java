package com.craftinginterpreters.lox;

import java.util.List;

// Abstract base class representing an expression in the Lox language.
abstract class Expr {

   // Visitor interface for implementing the Visitor pattern.
   // It allows different expression types to be processed in a type-safe manner.
   interface Visitor<R> {
      R visitBinaryExpr(Binary expr);
      R visitGroupingExpr(Grouping expr);
      R visitLiteralExpr(Literal expr);
      R visitUnaryExpr(Unary expr);
   }

   // Represents a binary expression (e.g., a + b).
   static class Binary extends Expr {
      Binary(Expr left, Token operator, Expr right) {
         this.left = left; // Left operand.
         this.operator = operator; // Operator (e.g., +, -, *, /).
         this.right = right; // Right operand.
      }

      // Accept method for visitor pattern implementation.
      @Override
      <R> R accept(Visitor<R> visitor) {
         return visitor.visitBinaryExpr(this);
      }

      final Expr left;
      final Token operator;
      final Expr right;
   }

   // Represents a grouping expression (e.g., (a + b)).
   static class Grouping extends Expr {
      Grouping(Expr expression) {
         this.expression = expression; // The contained expression.
      }

      // Accept method for visitor pattern.
      @Override
      <R> R accept(Visitor<R> visitor) {
         return visitor.visitGroupingExpr(this);
      }

      final Expr expression;
   }

   // Represents a literal value (e.g., numbers, strings, booleans).
   static class Literal extends Expr {
      Literal(Object value) {
         this.value = value; // The actual literal value.
      }

      // Accept method for visitor pattern.
      @Override
      <R> R accept(Visitor<R> visitor) {
         return visitor.visitLiteralExpr(this);
      }

      final Object value;
   }

   // Represents a unary expression (e.g., -a, !b).
   static class Unary extends Expr {
      Unary(Token operator, Expr right) {
         this.operator = operator; // Operator (e.g., - or !).
         this.right = right; // Operand.
      }

      // Accept method for visitor pattern.
      @Override
      <R> R accept(Visitor<R> visitor) {
         return visitor.visitUnaryExpr(this);
      }

      final Token operator;
      final Expr right;
   }

   // Abstract accept method that all subclasses must implement for visitor pattern.
   abstract <R> R accept(Visitor<R> visitor);
}
