package cn.edu.bnuz.bell.tm.here.api

import cn.edu.bnuz.bell.workflow.Activities
import cn.edu.bnuz.bell.workflow.Event
import cn.edu.bnuz.bell.workflow.State
import cn.edu.bnuz.bell.workflow.actions.AutoEntryAction
import cn.edu.bnuz.bell.workflow.actions.ManualEntryAction
import cn.edu.bnuz.bell.workflow.actions.SubmittedEntryAction
import cn.edu.bnuz.bell.workflow.config.StandardActionConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.statemachine.action.Action
import org.springframework.statemachine.config.EnableStateMachine
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer

@Configuration
@EnableStateMachine(name='studentLeaveFormsStateMachine')
@Import(StandardActionConfiguration)
class StudentLeaveStateMachineConfiguration extends EnumStateMachineConfigurerAdapter<State, Event> {
    @Autowired
    StandardActionConfiguration actions

    @Override
    void configure(StateMachineStateConfigurer<State, Event> states) throws Exception {
        states
            .withStates()
                .initial(State.CREATED)
                .state(State.CREATED,   [actions.logEntryAction()], null)
                .state(State.SUBMITTED, [actions.logEntryAction(), leaveSubmittedEntryAction()], [actions.workitemProcessedAction()])
                .state(State.REJECTED,  [actions.logEntryAction(), actions.rejectedEntryAction()], [actions.workitemProcessedAction()])
                .state(State.APPROVED,  [actions.logEntryAction(), leaveApprovedEntryAction()], [actions.workitemProcessedAction()])
                .state(State.FINISHED,  [actions.logEntryAction(), leaveFinishedEntryAction()], null)
                .state(State.CLOSED)
    }

    @Override
    void configure(StateMachineTransitionConfigurer<State, Event> transitions) throws Exception {
        transitions
            .withInternal()
                .source(State.CREATED)
                .event(Event.UPDATE)
                .action(actions.logTransitionAction())
                .and()
            .withExternal()
                .source(State.CREATED)
                .event(Event.SUBMIT)
                .target(State.SUBMITTED)
                .and()
            .withExternal()
                .source(State.SUBMITTED)
                .event(Event.ACCEPT)
                .target(State.APPROVED)
                .and()
            .withExternal()
                .source(State.SUBMITTED)
                .event(Event.REJECT)
                .target(State.REJECTED)
                .and()
            .withInternal()
                .source(State.REJECTED)
                .event(Event.UPDATE)
                .action(actions.logTransitionAction())
                .and()
            .withExternal()
                .source(State.REJECTED)
                .event(Event.SUBMIT)
                .target(State.SUBMITTED)
                .and()
            .withExternal() // 销假
                .source(State.APPROVED)
                .event(Event.FINISH)
                .target(State.FINISHED)
    }

    @Bean
    Action<State, Event> leaveSubmittedEntryAction() {
        new SubmittedEntryAction(Activities.APPROVE)
    }

    @Bean
    Action<State, Event> leaveApprovedEntryAction() {
        new ManualEntryAction('finish')
    }

    @Bean
    Action<State, Event> leaveFinishedEntryAction() {
        new AutoEntryAction()
    }
}
