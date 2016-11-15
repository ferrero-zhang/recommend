package com.ifeng.iRecommend.liuyi.customDicWordSearch;


/**
 * 基于双数组Trie树的AhoCorasick自动机
 */
public class PatternHit<V>
{
   
    public interface IHitFull<V>
    {
        /**
         * 命中一个模式串
         *
         * @param begin 模式串在母文本中的起始位置
         * @param end   模式串在母文本中的终止位置
         * @param value 模式串对应的值
         * @param index 模式串对应的值的下标
         */
        void hit(int begin, int end, V value, int index);
    }

    /**
     * 一个命中结果
     *
     * @param <V>
     */
    public class Hit<V>
    {
        /**
         * 模式串在母文本中的起始位置
         */
        public final int begin;
        /**
         * 模式串在母文本中的终止位置
         */
        public final int end;
        /**
         * 模式串对应的值
         */
        public final V value;

        public Hit(int begin, int end, V value)
        {
            this.begin = begin;
            this.end = end;
            this.value = value;
        }

        @Override
        public String toString()
        {
            return String.format("[%d:%d]=%s", begin, end, value);
        }
    }
    
    /**
     * 命中一个模式串的处理方法
     */
    public interface IHit<V>
    {
        /**
         * 命中一个模式串
         *
         * @param begin 模式串在母文本中的起始位置
         * @param end   模式串在母文本中的终止位置
         * @param value 模式串对应的值
         */
        void hit(int begin, int end, V value);
    }

}
