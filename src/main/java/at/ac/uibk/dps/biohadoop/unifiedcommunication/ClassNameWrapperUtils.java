package at.ac.uibk.dps.biohadoop.unifiedcommunication;

import at.ac.uibk.dps.biohadoop.communication.Message;
import at.ac.uibk.dps.biohadoop.communication.MessageType;
import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.queue.TaskId;

public class ClassNameWrapperUtils {

//	public static <T> Message<ClassNameWrapper<T>> wrapMessage(
//			Message<T> inputMessage, String className) {
//		Task<T> inputTask = inputMessage.getTask();
//		ClassNameWrapper<T> wrapper = new ClassNameWrapper<T>(className,
//				inputTask.getData());
//		Task<ClassNameWrapper<T>> outputTask = new Task<ClassNameWrapper<T>>(
//				inputTask.getTaskId(), wrapper);
//		return new Message<ClassNameWrapper<T>>(inputMessage.getType(),
//				outputTask);
//	}
//
//	public static <T> Message<ClassNameWrapper<T>> getMessage(MessageType messageType, TaskId taskId,
//			String className, T data) {
//		ClassNameWrapper<T> wrapper = new ClassNameWrapper<T>(className,
//				data);
//		ClassNameWrappedTask<T> wrappedTask = new ClassNameWrappedTask<T>();
//		Message<ClassNameWrapper<T>> outputMessage = new Message<ClassNameWrapper<T>>(MessageType.REGISTRATION_REQUEST, wrappedTask)
//	}
//
//	public static <T> Message<T> unwrapMessage(
//			Message<ClassNameWrapper<T>> inputMessage) {
//		Task<ClassNameWrapper<T>> inputTask = inputMessage.getTask();
//		Task<T> outputTask = new Task<T>(inputTask.getTaskId(), inputTask
//				.getData().getWrapped());
//		return new Message<T>(inputMessage.getType(), outputTask);
//	}

}
