package cn.edu.bnuz.bell.tm.here.api

import cn.edu.bnuz.bell.here.FreeListenReviewerService
import cn.edu.bnuz.bell.here.StudentLeaveReviewerService
import cn.edu.bnuz.bell.workflow.DomainStateMachineHandler
import cn.edu.bnuz.bell.workflow.Event
import cn.edu.bnuz.bell.workflow.State
import cn.edu.bnuz.bell.workflow.StateObject
import cn.edu.bnuz.bell.workflow.config.DefaultStateMachineConfiguration
import cn.edu.bnuz.bell.workflow.config.DefaultStateMachinePersistConfiguration
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.statemachine.StateMachine
import org.springframework.statemachine.persist.StateMachinePersister

@Configuration
@Import([
        StudentLeaveStateMachineConfiguration,
        DefaultStateMachineConfiguration,
        DefaultStateMachinePersistConfiguration,
])
class WorkflowConfiguration {
    @Bean('freeListenFormStateHandler')
    DomainStateMachineHandler freeListenFormStateHandler(
            @Qualifier('defaultStateMachine')
            StateMachine<State, Event> stateMachine,
            StateMachinePersister<State, Event, StateObject> persister,
            FreeListenReviewerService reviewerService) {
        new DomainStateMachineHandler(stateMachine, persister, reviewerService)
    }

    @Bean('studentLeaveFormStateHandler')
    DomainStateMachineHandler studentLeaveFormStateHandler(
            @Qualifier('studentLeaveFormsStateMachine')
            StateMachine<State, Event> stateMachine,
            StateMachinePersister<State, Event, StateObject> persister,
            StudentLeaveReviewerService reviewerService) {
        new DomainStateMachineHandler(stateMachine, persister, reviewerService)
    }
}