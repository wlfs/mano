/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

/**
 *
 * @author jun
 */
public class HttpPhase {

	/**
	 * 
	 */
	public final static int None=0;
	/**
	 * 
	 */
	public final static int Queued=1;
	/**
	 * 解析请求行
	 */
	public final static int ResolveLine=2;
	/**
	 * 解析协议头
	 */
	public final static int ResolveHeader=3;
	/**
	 * 解析主体
	 */
	public final static int ResolveBody=4;
	/**
	 * 加载显示大小的主体数据
	 */
	public final static int LoadExactData=5;
	/**
	 * 解析块头
	 */
	public final static int ResolveChunkedHeader=6;
	/**
	 * 解析块数据
	 */
	public final static int LoadChunkedData=7;
	/**
	 * waits 100ms 加载多余数据
	 */
	public final static int IgnoreExtraData=8;
	/**
	 * 准备处理器
	 */
	public final static int PreHandler=9;
	/**
	 * 处理
	 */
	public final static int PostHandler=10;
	/**
	 *  发送队列数据
	 */
	public final static int SendRespenseQueuedData=11;
	/**
	 * 完成
	 */
	public final static int Finish=12;
	/**
	 * 错误
	 */
	public final static int Error=13;
    
}
