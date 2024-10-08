package com.intbyte.wizbuddy.taskperchecklist.command.application.service;

import com.intbyte.wizbuddy.common.exception.CommonException;
import com.intbyte.wizbuddy.common.exception.StatusEnum;
import com.intbyte.wizbuddy.taskperchecklist.command.application.dto.TaskPerCheckListDTO;
import com.intbyte.wizbuddy.taskperchecklist.command.domain.aggregate.entity.TaskPerCheckList;
import com.intbyte.wizbuddy.taskperchecklist.command.domain.aggregate.entity.TaskPerCheckListId;
import com.intbyte.wizbuddy.taskperchecklist.command.domain.repository.TaskPerCheckListRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppTaskPerCheckListServiceImpl implements AppTaskPerCheckListService {

    private final TaskPerCheckListRepository taskPerCheckListRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public AppTaskPerCheckListServiceImpl(TaskPerCheckListRepository taskPerCheckListRepository, ModelMapper modelMapper) {
        this.taskPerCheckListRepository = taskPerCheckListRepository;
        this.modelMapper = modelMapper;
    }

    // checklist infra command 1에서 호출. 특정 체크리스트에 여러개의 특정 업무 추가
    @Override
    @Transactional
    public void insertTaskPerCheckListList(List<Integer> taskCodeList, int shopCode) {

        for (int i = 0; i < taskCodeList.size(); i++) {
            TaskPerCheckList taskPerCheckList = TaskPerCheckList.builder()
                    .checkListCode(shopCode)
                    .taskCode(taskCodeList.get(i))
                    .taskFinishedState(false)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            taskPerCheckListRepository.save(taskPerCheckList);
        }
    }

    // checklist infra command 2에서 호출 -> 특정 체크리스트에 특정 업무 추가 -> 이걸 수정해야겠다. -> 추가하는 경우에는 employeeCode는 필요없기때문
    @Override
    @Transactional
    public void insertTaskPerCheckList(int checkListCode, int taskCode) {

        TaskPerCheckListId taskPerChecklistId = new TaskPerCheckListId(checkListCode, taskCode);

        TaskPerCheckList taskPerCheckList = TaskPerCheckList.builder()
                .checkListCode(checkListCode)
                .taskCode(taskCode)
                .taskFinishedState(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        taskPerCheckListRepository.save(taskPerCheckList);
    }

    // checklist infra command 3에서 호출. 특정 체크리스트에 존재하는 특정 업무 삭제
    @Override
    @Transactional
    public void deleteTaskPerCheckListByCheckListCodeAndTaskCode(int checkListCode, int taskCode){

        TaskPerCheckListId id = new TaskPerCheckListId(checkListCode, taskCode);

        // DB에 없는경우 예외처리
        taskPerCheckListRepository.findById(id)
                .orElseThrow(() -> new CommonException(StatusEnum.TASK_PER_CHECKLIST_NOT_FOUND));

        taskPerCheckListRepository.deleteById(id);
    }


    // checklist infra command 5에서 호출. 특정 chekcListCode에 속하는 모든 Task 삭제하는 메서드
    @Override
    @Transactional
    public void deleteTaskPerCheckListByCheckListCode(int checkListCode){

        taskPerCheckListRepository.deleteByCheckListCode(checkListCode);
    }

    // task infra command 3에서 호출
    @Override
    @Transactional
    public void deleteTaskPerCheckListByTaskCode(int taskCode) {

        taskPerCheckListRepository.deleteByTaskCode(taskCode);
    }


    // command 1. 특정 체크리스트에 특정 업무 완료표시
    @Override
    @Transactional
    public void modifyTaskPerCheckList(TaskPerCheckListDTO dto){

        TaskPerCheckList taskPerCheckList = taskPerCheckListRepository.findById
                (new TaskPerCheckListId(dto.getCheckListCode(), dto.getTaskCode()))
                .orElseThrow(() -> new CommonException(StatusEnum.TASK_PER_CHECKLIST_NOT_FOUND));

        taskPerCheckList.modify(dto, dto.getEmployeeCode());

        taskPerCheckListRepository.save(taskPerCheckList);
    }

    // id와 repository로 존재하는지 유효성검사
    @Override
    @Transactional
    public boolean findById(int checkListCode, int taskCode){
        if(taskPerCheckListRepository.findById(new TaskPerCheckListId(checkListCode, taskCode)) != null) return true;
        else return false;
    }

}
