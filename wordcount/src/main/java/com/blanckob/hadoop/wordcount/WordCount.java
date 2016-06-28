package com.blanckob.hadoop.wordcount;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class WordCount {
	
	
	/*	입력 데이터
	 * 	0. hello monkey hello
	 * 	19. bear car
	 *  출력 데이터
	 *  hello, 1
	 *  monkey, 1
	 *  hello, 1
	 *  bear, 1
	 *  car, 1
	 */
	
	public static class MyMapper extends Mapper<LongWritable, Text, Text, LongWritable>{

		@Override
		protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, Text, LongWritable>.Context context)
				throws IOException, InterruptedException {
			String line = value.toString();
			StringTokenizer st = new StringTokenizer(line, " \t\n\r\f|,.()<>{}\"\'");
			while(st.hasMoreTokens()){
				String word = st.nextToken();
				
				Text outKey = new Text(word);
				LongWritable outValue = new LongWritable(1L);
				
				context.write(outKey, outValue);
			}
		}
		
	}
	
	/*
	 * 입력 데이터
	 *  hello, 1
	 *  monkey, 1
	 *  hello, 1
	 *  bear, 1
	 *  car, 1
	 * 출력 데이터
	 *  hello, 2
	 *  monkey, 1
	 *  bear,1 
	 *  car ,1
	 */
	
	public static class MyReduce extends Reducer<Text, LongWritable, Text, LongWritable>{

		@Override
		protected void reduce(Text key, Iterable<LongWritable> values,
				Reducer<Text, LongWritable, Text, LongWritable>.Context context) throws IOException, InterruptedException {
			long count = 0;
			for(LongWritable val : values){
				count += val.get();
			}
			context.write(key, new LongWritable(count));
		}

	}
	
	// input output를 프로그램의 인자로 입력받음
	public static void main(String args[]) throws Exception{
		Configuration conf = new Configuration();
		Job job = new Job(conf);
		
		job.setJarByClass(WordCount.class);
		
		//매퍼클래스 지정
		job.setMapperClass(WordCount.MyMapper.class);
		
		//리듀서 클래스 지정
		job.setReducerClass(WordCount.MyReduce.class);
		
		//최종 결과물 출력 타입 지정 (리듀서 결과물 - > 파일)
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(LongWritable.class);
		
		//입력 데이터 처리 클래스 지정( 입력 파일 - > 매퍼 )
		job.setInputFormatClass(TextInputFormat.class);
		
		//출력 데이터 처리 클래스 지정
		job.setOutputFormatClass(TextOutputFormat.class);
		
		// 입력 파일 정보 (여러개의 입력이 와도 됨)
		FileInputFormat.addInputPath(job, new Path(args[0]));
		//FileInputFormat.addInputPaths(arg0, arg1); 한번에 여러 입력 파일 경로 지정.
		// 출력 파일 정보
		FileOutputFormat.setOutputPath(job, new Path(args[1]));		
		
		
		//JOb실행
		job.waitForCompletion(true); // job 완료 시 까지 대기 후 프로그램 종료
		
		
	}

}
