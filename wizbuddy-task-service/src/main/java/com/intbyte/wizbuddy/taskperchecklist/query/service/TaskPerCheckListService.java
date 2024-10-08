package com.intbyte.wizbuddy.taskperchecklist.query.service;

import com.intbyte.wizbuddy.taskperchecklist.query.dto.TaskPerCheckListQueryDTO;

import java.util.List;

public interface TaskPerCheckListService {
    // 1-1. 특정 체크리스트에 존재하는 모든 완료된 업무 조회
    List<TaskPerCheckListQueryDTO> findAllTaskPerCheckListByCheckListCodeByFinished(int checkListCode);

    // 1-2. 특정 체크리스트에 존재하는 모든 미완료된 업무 조회
    List<TaskPerCheckListQueryDTO> findAllTaskPerCheckListByCheckListCodeByNotFinished(int checkListCode);

    // 1-3. 특정 체크리스트에 존재하는 모든 업무 조회(완료 + 미완료)
    List<TaskPerCheckListQueryDTO> findAllTaskPerCheckListByCheckListCode(int checkListCode);
}
