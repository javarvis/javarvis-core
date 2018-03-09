/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:  
 *       "This product includes software developed by the 
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */ 

package com.itineric.javarvis.core.automation.commandanalysis;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 *
 * <p>The implementation of the plus operator
 * 
 * @author Nathan Abramson - Art Technology Group
 * @version $Change: 181177 $$DateTime: 2001/06/26 08:45:09 $$Author: luehe $
 **/

public class PlusOperator
  extends ArithmeticOperator
{
  //-------------------------------------
  // Singleton
  //-------------------------------------

  public static final PlusOperator SINGLETON =
    new PlusOperator ();

  //-------------------------------------
  /**
   *
   * Constructor
   **/
  public PlusOperator ()
  {
  }

  //-------------------------------------
  // Expression methods
  //-------------------------------------
  /**
   *
   * Returns the symbol representing the operator
   **/
  public String getOperatorSymbol () {
    return "+";
  }

  //-------------------------------------
  /**
   *
   * Applies the operator to the given double values, returning a double
   **/
  public double apply (double pLeft, double pRight) {
    return pLeft + pRight;
  }
  
  //-------------------------------------
  /**
   *
   * Applies the operator to the given double values, returning a double
   **/
  public long apply (long pLeft, long pRight) {
    return pLeft + pRight;
  }
  
  //-------------------------------------

    /**
     *
     * Applies the operator to the given BigDecimal values, returning a
     * BigDecimal.
     **/
    public BigDecimal apply(BigDecimal pLeft, BigDecimal pRight) {
        return pLeft.add(pRight);
    }

    //-------------------------------------

    /**
     *
     * Applies the operator to the given BigInteger values, returning a
     * BigInteger.
     **/
    public BigInteger apply(BigInteger pLeft, BigInteger pRight) {
        return pLeft.add(pRight);
    }

    //-------------------------------------
}
