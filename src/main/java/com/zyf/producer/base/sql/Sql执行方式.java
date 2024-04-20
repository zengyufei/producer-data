package com.zyf.producer.base.sql;

import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.EventHandlerGroup;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Sql执行方式<T extends BaseSqlContext> {

    @Getter
    private final Disruptor<T> disruptor;
    @Getter
    @Setter
    private int total = 0;

    public Sql执行方式(Disruptor<T> disruptor) {
        this.disruptor = disruptor;
    }


    public 串联重复消费下一步执行方式<T> 串联重复消费(公共Sql消费者<T>... handlers) {
        total += handlers.length;
        return new 串联重复消费下一步执行方式<T>(disruptor.handleEventsWith(handlers));
    }

    public 串联重复多次下一步执行方式<T> 串联重复消费(List<公共Sql消费者<T>[]> handlerList) {
        List<EventHandlerGroup<T>> list = new ArrayList<>();
        for (公共Sql消费者<T>[] eventHandlerAttr : handlerList) {
            total += eventHandlerAttr.length;
            list.add(disruptor.handleEventsWith(eventHandlerAttr));
        }
        return new 串联重复多次下一步执行方式<T>(list);
    }


    public 并发重复多次下一步执行方式<T> 并发消费(List<公共Sql消费者<T>[]> handlerList) {
        Set<公共Sql消费者<T>[]> handlerSet = new HashSet<>(handlerList);
        if (handlerSet.size() != handlerList.size()) {
            throw new RuntimeException("不允许数组重复add");
        }

        List<EventHandlerGroup<T>> list = new ArrayList<>();
        for (公共Sql消费者<T>[] workHandlerAttr : handlerList) {
            total += workHandlerAttr.length;
            list.add(disruptor.handleEventsWithWorkerPool(workHandlerAttr));
        }
        return new 并发重复多次下一步执行方式<>(list);
    }

    public class 串联重复消费下一步执行方式<T extends BaseSqlContext> {
        private EventHandlerGroup<T> eventHandlerGroup;

        public 串联重复消费下一步执行方式(EventHandlerGroup<T> eventHandlerGroup) {
            this.eventHandlerGroup = eventHandlerGroup;
        }

        public 串联重复消费下一步执行方式<T> 串联重复消费(公共Sql消费者<T>... handlers) {
            total += handlers.length;
            return new 串联重复消费下一步执行方式<T>(eventHandlerGroup.then(handlers));
        }

        public 串联重复多次下一步执行方式<T> 串联重复消费(List<公共Sql消费者<T>[]> handlerList) {
            List<EventHandlerGroup<T>> list = new ArrayList<>();
            for (公共Sql消费者<T>[] eventHandlerAttr : handlerList) {
                total += eventHandlerAttr.length;
                list.add(eventHandlerGroup.then(eventHandlerAttr));
            }
            return new 串联重复多次下一步执行方式<T>(list);
        }

    }

    public class 串联重复多次下一步执行方式<T extends BaseSqlContext> {
        private List<EventHandlerGroup<T>> eventHandlerGroups;

        public 串联重复多次下一步执行方式(List<EventHandlerGroup<T>> list) {
            this.eventHandlerGroups = list;
        }

        public 串联重复多次下一步执行方式<T> 串联重复消费(公共Sql消费者<T>... handlers) {
            total += handlers.length;
            List<EventHandlerGroup<T>> list = new ArrayList<>();
            for (EventHandlerGroup<T> handlerGroup : eventHandlerGroups) {
                final EventHandlerGroup<T> tEventHandlerGroup = handlerGroup.then(handlers);
                list.add(tEventHandlerGroup);
            }
            return new 串联重复多次下一步执行方式<T>(list);
        }

        public 串联重复多次下一步执行方式<T> 串联重复消费(List<公共Sql消费者<T>[]> handlerList) {
            List<EventHandlerGroup<T>> list = new ArrayList<>();
            for (EventHandlerGroup<T> handlerGroup : eventHandlerGroups) {
                EventHandlerGroup<T> childGroup = handlerGroup;
                for (公共Sql消费者<T>[] eventHandlerAttr : handlerList) {
                    total += eventHandlerAttr.length;
                    childGroup = childGroup.then(eventHandlerAttr);
                }
                list.add(childGroup);
            }
            return new 串联重复多次下一步执行方式<T>(list);
        }

    }


    public class 并发重复多次下一步执行方式<T extends BaseSqlContext> {
        private List<EventHandlerGroup<T>> eventHandlerGroups;

        public 并发重复多次下一步执行方式(List<EventHandlerGroup<T>> list) {
            this.eventHandlerGroups = list;
        }


        public 并发重复多次下一步执行方式<T> 并发消费(List<公共Sql消费者<T>[]> handlerList) {
            Set<公共Sql消费者<T>[]> handlerSet = new HashSet<>(handlerList);
            if (handlerSet.size() != handlerList.size()) {
                throw new RuntimeException("不允许数组重复add");
            }

            for (公共Sql消费者<T>[] handlers : handlerList) {
                total += handlers.length;
                EventHandlerGroup<T> childGroup = eventHandlerGroups.remove(0);
                childGroup = childGroup.thenHandleEventsWithWorkerPool(handlers);
                eventHandlerGroups.add(childGroup);
            }
            return new 并发重复多次下一步执行方式<T>(eventHandlerGroups);
        }

    }

}
