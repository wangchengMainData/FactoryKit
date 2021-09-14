/*
 * Copyright (c) 2011-2014, Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */
package com.gosuncn.zfyfactorytest.MSensor;
import android.view.animation.Animation;
import android.view.animation.Transformation;

 class CompassRotation extends Animation {
	private float startAngle;
	private float endAngle;
	private int width;
	private int height;

	@Override
	public void initialize(int width,int height,int parentWidth,int parentHeight){
		super.initialize(width,height,parentWidth,parentHeight);
		this.width = width;
		this.height = height;
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t)
	{
		super.applyTransformation(interpolatedTime,t);
		float angleDifference = endAngle - startAngle;
		t.getMatrix().setRotate(startAngle + angleDifference * interpolatedTime,width/2,height/2);
	}

	public float getStartAngle(){
		return this.startAngle;
	}

	public void setStartAngle(float startAngle){
		this.startAngle = startAngle;
	}
	public float getEndAngle(){
		return this.endAngle;
	}

	public void setEndAngle(float endAngle){
		this.endAngle = endAngle;
	}


}
